//--------------------------------------------------
// Interface BotAuth
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.middleware.auth

import com.kenvix.moecraftbot.ng.Defines
import com.kenvix.moecraftbot.ng.lib.BotCommandCallback
import com.kenvix.moecraftbot.ng.lib.CHAT_TYPE_AUTH
import com.kenvix.moecraftbot.ng.lib.bot.*
import com.kenvix.moecraftbot.ng.lib.dao.AuthsDao
import com.kenvix.moecraftbot.ng.lib.exception.BusinessLogicException
import com.kenvix.moecraftbot.ng.lib.exception.InvalidAuthorizationException
import com.kenvix.moecraftbot.ng.lib.exception.UserViolationException
import com.kenvix.moecraftbot.ng.lib.middleware.BotMiddleware
import com.kenvix.moecraftbot.ng.lib.nameAndHashcode
import com.kenvix.moecraftbot.ng.lib.replacePlaceholders
import com.kenvix.utils.log.Logging
import com.kenvix.utils.log.severe
import java.io.IOException
import java.sql.Timestamp

class BotAuth(val authCallback: BotAuthCallback) : BotMiddleware, Logging {
    override fun getLogTag(): String = "BotAuth"

    private val commands = mapOf<String, BotCommandCallback>(
        "start" to ::onAuthCommandStart,
        "info" to ::onAuthInfoQuery,
        "cancelAuth" to ::onAuthCommandCancel,
        "backAuth" to ::onAuthCommandBack,
        "retryAuth" to ::onAuthCommandRetry,
        "statAuth" to ::onStatAuth
    )

    private val authSessions = mutableMapOf<Long, BotAuthSession>()
    private lateinit var botName: String
    private lateinit var driverContext: AbstractDriver<*>
    private lateinit var botProvider: AbstractBotProvider<*>

    override fun onEnable(driverContext: AbstractDriver<*>) {
        driverContext.registerCommandGroup(commands)
        this.driverContext = driverContext
        botName = driverContext.botName
        botProvider = this.driverContext.botProvider
        authCallback.onEnable(driverContext)
    }

    private fun onAuthCommandStart(update: BotUpdate<*>, commandQueryData: BotCommandQueryData) {
        if (update.isUserMessage && update.message!!.messageFrom == MessageFrom.Private) {
            val sessionBot: BotAuthSession
            val userId = update.fromUserId

            if (authSessions.containsKey(userId) && authSessions[userId] != null) {
                sessionBot = authSessions[userId]!!
            } else {
                sessionBot = BotAuthSession(userId)
                authSessions[userId] = sessionBot
            }
            driverContext.setChatSession(update, ChatSession(CHAT_TYPE_AUTH, sessionBot))
            logger.finest("Start auth session for user $userId ")
            authCallback.onAuthSessionBegin(update, commandQueryData, sessionBot)

            requireUsername(update, sessionBot)
        }
    }

    private fun requireUsername(update: BotUpdate<*>, authSession: BotAuthSession) {
        botProvider.sendMessage(update, "欢迎使用 $botName 认证管理工具\n请输入您在本站的用户名或邮箱")
        authSession.status = BotAuthStatus.WaitingUsername
    }

    private fun onAuthCommandBack(update: BotUpdate<*>, @Suppress("UNUSED_PARAMETER") commandQueryData: BotCommandQueryData) {
        if (update.isUserMessage && update.message!!.messageFrom == MessageFrom.Private && driverContext.containsChatSession(update)) {
            val chatSession: ChatSession = driverContext.getChatSession(update) ?: return

            if (chatSession.chatTypeCode == CHAT_TYPE_AUTH) {
                val authSession: BotAuthSession = chatSession.extraData as BotAuthSession

                when(authSession.status) {
                    BotAuthStatus.WaitingPassword -> {
                        requireUsername(update, authSession)
                    }
                    BotAuthStatus.Authenticating -> {
                        requirePassword(update, authSession)
                    }
                    //Invalid state
                    else -> {
                        driverContext.botProvider.sendMessage(update, "已回退到认证会话顶层，不能继续回退")
                        return
                    }
                }
            }
        }
    }

    private fun onAuthCommandCancel(update: BotUpdate<*>, @Suppress("UNUSED_PARAMETER") commandQueryData: BotCommandQueryData) {
        if (update.isUserMessage && update.message!!.messageFrom == MessageFrom.Private && driverContext.containsChatSession(update)) {
            val chatSession: ChatSession = driverContext.getChatSession(update) ?: return

            if (chatSession.chatTypeCode == CHAT_TYPE_AUTH) {
                driverContext.removeChatSession(update)
                driverContext.botProvider.sendMessage(update, "认证会话取消成功")
            }
        }
    }

    private fun onAuthCommandRetry(update: BotUpdate<*>, @Suppress("UNUSED_PARAMETER") commandQueryData: BotCommandQueryData) {
        if (update.isUserMessage && update.message!!.messageFrom == MessageFrom.Private && driverContext.containsChatSession(update)) {
            val chatSession: ChatSession = driverContext.getChatSession(update) ?: return

            if (chatSession.chatTypeCode == CHAT_TYPE_AUTH) {
                val authSession: BotAuthSession = chatSession.extraData as BotAuthSession

                if (authSession.status == BotAuthStatus.Authenticating)
                    runAuth(update, authSession)
                else
                    driverContext.botProvider.sendMessageNoException(update, "会话状态不正确，请输入 /start 重新开始认证")
            }
        }
    }

    private fun onStatAuth(update: BotUpdate<*>, @Suppress("UNUSED_PARAMETER") commandQueryData: BotCommandQueryData) {
        if (update.isUserMessage && update.message!!.messageFrom == MessageFrom.Group) {
            val message = StringBuilder("Chat ID: ${update.chatId}\n")

            message.append("Authorizer Status: ")
            message.append(if(Defines.systemOptions.auth.groups.contains(update.chatId)) "Enabled\n" else "Disabled\n")

            message.append("User count: ")
            message.append(AuthsDao.countAll())
            message.append("\n")

            driverContext.botProvider.sendMessage(update, message.toString())
        }
    }

    fun onMessage(update: BotUpdate<*>, message: String): Boolean {
        if (update.hasMessage) {
            if (update.isUserMessage && update.message!!.messageFrom == MessageFrom.Private && driverContext.containsChatSession(update)) {
                return handleAuthPrivateMessage(update, message)
            }
        }

        return true
    }

    fun onEvent(update: BotUpdate<*>, eventType: MessageType) {
        if (eventType == MessageType.EventGroupMembersJoin) {
            handleAuthMemberJoin(update)
        }
    }

    private fun onAuthInfoQuery(update: BotUpdate<*>, commandQueryData: BotCommandQueryData) {
        if (update.isUserMessage && update.message!!.messageFrom == MessageFrom.Group
            && Defines.systemOptions.auth.admins.contains(update.fromUserId)) {

            try {
                if (update.message!!.replyToMessage == null) {
                    throw UserViolationException("请回复一条消息")
                }

                val target = update.message!!.replyToMessage!!.sender
                if (target != null) {
                    if (Defines.systemOptions.auth.admins.contains(target.id))
                        throw UserViolationException("不允许查询管理员的信息")

                    val authInfo = AuthsDao.fetchOneByTgUserId(target.id.toInt()) ?: throw UserViolationException("用户身份验证信息不存在")


                    val resultBuilder = StringBuilder("用户身份信息查询：\n$target\n=============================")
                    resultBuilder.append("站点 #${authInfo.siteUserId} ${authInfo.siteUserName}\n")

                    authCallback.onAuthInfoQuery(update, commandQueryData, authInfo, resultBuilder)
                    driverContext.botProvider.sendMessage(update, resultBuilder.toString(), update.message!!.id)
                } else {
                    throw UserViolationException("无法获取目标用户")
                }
            } catch (e: UserViolationException) {
                driverContext.botProvider.sendMessage(update, "错误：${e.message}", update.message!!.id)
            }
        }
    }

    private fun handleAuthMemberJoin(update: BotUpdate<*>) {
        if (!(update.message == null || update.message!!.messageType != MessageType.EventGroupMembersJoin || update.message!!.extraData == null)) {
            if (Defines.systemOptions.auth.groups.contains(update.chatId)) {
                val users = update.message!!.extraData!!.users!!

                for (user in users) {
                    try {
                        if (update.message!!.sender != null && Defines.systemOptions.auth.admins.contains(update.message!!.sender!!.id)) {
                            logger.fine("Admin " + update.message!!.sender + " invited user " + user + " to join group " + update.chatId)
                            driverContext.botProvider.sendMessage(update, "管理员通过特权邀请用户 ${user.name} ${user.description} 加入本群")
                        } else {
                            val authInfo = AuthsDao.fetchOneByTgUserId(user.id.toInt()) ?: throw InvalidAuthorizationException()

                            if (!isAuthenticated(authInfo.level))
                                throw InvalidAuthorizationException()

                            authCallback.onNewMemberJoin(update, user, authInfo)

                            //Welcome
                            driverContext.botProvider.sendMessage(update, Defines.systemOptions.auth.welcomeMessage.replacePlaceholders(mapOf(
                                "siteUserName" to authInfo.siteUserName,
                                "tgUserName" to authInfo.tgUserName,
                                "tgUserDescription" to authInfo.tgUserDescription
                            )))
                        }
                    } catch (e: BusinessLogicException) {
                        //Kick user
                        try {
                            driverContext.botProvider.kickUser(update.chatId, userId = user.id)

                            if (e !is InvalidAuthorizationException) {
                                logger.fine("Kicked user $user from group ${update.chatId} due to ${e.message}")
                                driverContext.botProvider.sendMessage(update, "已踢出 $user，因为 ${e.message}")
                            } else {
                                logger.fine("Kicked illegal user $user from group ${update.chatId}")
                            }
                        } catch (e2: Exception) {
                            val error = "错误：未能踢出非法用户 $user 加群 ${update.chatId}: " + e.toString()
                            logger.severe(e, error)
                            driverContext.botProvider.sendMessage(update, error)
                        }
                    } catch (e: Exception) {
                        //Kick FAILED
                        val error = "无法处理用户 $user 加群 ${update.chatId} 的认证: "
                        logger.severe(e, error)
                        driverContext.botProvider.sendMessage(update, error + e.nameAndHashcode)
                    }
                }
            }
        }
        //TODO: FOR QQ
    }

    private fun handleAuthPrivateMessage(update: BotUpdate<*>, message: String): Boolean {
        val chatSession: ChatSession = driverContext.getChatSession(update) ?: return false

        if (chatSession.chatTypeCode == CHAT_TYPE_AUTH) {
            val authSession: BotAuthSession = chatSession.extraData as BotAuthSession

            when (authSession.status) {
                //To Save Username
                BotAuthStatus.WaitingUsername -> {
                    authSession.data.email = message

                    requirePassword(update, authSession)
                }
                //To Save Password and START Auth
                BotAuthStatus.WaitingPassword -> {
                    authSession.data.password = message

                    authSession.status = BotAuthStatus.Authenticating
                    runAuth(update, authSession)
                }
                BotAuthStatus.Authenticating -> {
                    runAuth(update, authSession)
                }
                //Invalid state
                else -> {
                    driverContext.removeChatSession(update)
                    driverContext.botProvider.sendMessage(update, "会话状态不正确，请输入 /start 重新开始认证")
                }
            }
        }

        return true
    }

    private fun requirePassword(update: BotUpdate<*>, authSession: BotAuthSession) {
        driverContext.botProvider.sendMessage(update, "接下来，请输入您的密码：\n"
                    + "若刚才的用户名输入错误，请输入 /backAuth")

        authSession.status = BotAuthStatus.WaitingPassword
    }

    private fun runAuth(update: BotUpdate<*>, authSession: BotAuthSession) {
        val loadingMessage = driverContext.botProvider.sendMessage(update, "正在进行认证，请稍候")

        try {
            val recordByTelegramId = AuthsDao.fetchOneByTgUserId(update.fromUserId.toInt())

            if (recordByTelegramId != null && recordByTelegramId.level == AuthenticatedUserLevel.BANNED.levelCode)
                throw InvalidAuthorizationException("Banned user by TelegramID")

            authCallback.onAuthSiteRequest(update, authSession)

            if (authSession.data.name == null || authSession.data.uid == null)
                throw IOException("认证服务器未返回数据")

            if (recordByTelegramId != null) {
                AuthsDao.updateSiteInfoById(recordByTelegramId.id, authSession.data)
            } else {
                if (Defines.systemOptions.auth.disallowSiteUserToMultipleOneTgAccount) {
                    val existSiteUser = AuthsDao.fetchOneBySiteUserId(authSession.data.uid)

                    if (existSiteUser != null) {
                        if (!Defines.systemOptions.auth.admins.contains(update.message!!.sender!!.id)
                            && !Defines.systemOptions.auth.admins.contains(existSiteUser.tgUserId.toLong())) {
                            try {
                                driverContext.botProvider.kickUser(update.chatId, existSiteUser.tgUserId.toLong())
                                logger.fine("""Kicked old user $existSiteUser due to new tg account ${update.message!!.sender} authorized (site account: ${authSession.data})""")
                            } catch (e: Exception) {
                                val message = "未能踢出更换了IM账号的用户 ${authSession.userName}. \n需要踢出的账号：${update.message!!.sender}"
                                logger.severe(e, message)
                            }
                        } else {
                            logger.fine("Admin auth, will not kick old user $existSiteUser")
                        }

                        existSiteUser.tgUserId = update.fromUserId.toInt()
                        existSiteUser.tgUserDescription = update.message!!.sender!!.description
                        existSiteUser.tgUserName = update.message!!.sender!!.description
                        existSiteUser.siteUserName = authSession.data.name ?: ""
                        existSiteUser.siteUserToken =  authSession.data.token ?: ""
                        existSiteUser.authedAt = Timestamp(System.currentTimeMillis())

                        AuthsDao.update(existSiteUser)
                    } else {
                        AuthsDao.addAuth(authSession, update.message!!.sender!!)
                    }
                } else {
                    AuthsDao.addAuth(authSession, update.message!!.sender!!)
                }
            }

            logger.fine("Successfully Authorized user ${update.message!!.sender}")
            authCallback.onAuthSessionSuccessfullyEnd(update, authSession)

            if (!Defines.systemOptions.auth.groups.isNullOrEmpty()) {
                for (groupChatId in Defines.systemOptions.auth.groups) {
                    try {
                        driverContext.botProvider.unbanUser(chatId = groupChatId, userId = update.fromUserId)
                    } catch (e: Exception) {
                        logger.info("Unban user chatId: $groupChatId, userId: ${update.fromUserId} failed: " + e.toString())
                    }
                }
            }

            driverContext.botProvider.sendMessage(update, Defines.systemOptions.auth.successMessage.replacePlaceholders(mapOf(
                "siteUserName" to authSession.data.name!!,
                "tgUserName" to update.message!!.sender!!.name,
                "tgUserDescription" to update.message!!.sender!!.description
            )))

            authSession.status = BotAuthStatus.Done
            driverContext.removeChatSession(update)
        } catch (e: InvalidAuthorizationException) {
            authSession.status = BotAuthStatus.WaitingPassword

            driverContext.botProvider.sendMessage(update, "您的用户名或密码输入有误。\n"
                    + "若要重新输入密码，请直接发送新密码。若要重新输入用户名，请输入 /backAuth")
        } catch (e: UserViolationException) {
            authSession.status = BotAuthStatus.WaitingPassword

            driverContext.botProvider.sendMessage(update,  "${e.message}\n"
                    + "若要重试，请输入 /retryAuth")
        } catch (e: Exception) {
            logger.warning("User auth failed: $e")

            driverContext.botProvider.sendMessage(update, "错误：${e.nameAndHashcode}\n"
                    + "若要重试，请输入 /retryAuth")
        } finally {
            driverContext.botProvider.deleteMessage(update.chatId, loadingMessage.id)
        }
    }
}