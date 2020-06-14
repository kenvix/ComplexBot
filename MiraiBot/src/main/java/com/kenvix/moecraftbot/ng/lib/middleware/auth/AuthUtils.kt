package com.kenvix.moecraftbot.ng.lib.middleware.auth

import com.kenvix.moecraftbot.ng.lib.bot.AbstractDriver
import com.kenvix.moecraftbot.ng.lib.bot.BotUpdate
import com.kenvix.moecraftbot.ng.lib.bot.MessageFrom
import com.kenvix.moecraftbot.ng.lib.dao.AuthsDao
import com.kenvix.moecraftbot.ng.lib.exception.InvalidAuthorizationException
import com.kenvix.moecraftbot.ng.lib.exception.UserViolationException
import com.kenvix.moecraftbot.ng.orm.tables.pojos.Auths

fun isAuthenticated(code: Byte): Boolean = code == AuthenticatedUserLevel.USER.levelCode || code == AuthenticatedUserLevel.ADMIN.levelCode

@Throws(InvalidAuthorizationException::class)
fun getAuthInfoByTgId(tgId: Int): Auths {
    val record = AuthsDao.fetchOneByTgUserId(tgId) ?: throw InvalidAuthorizationException("您必须先*私聊*使用 /start 命令进行身份认证后才能使用此命令")

    if (!isAuthenticated(record.level))
        throw InvalidAuthorizationException("身份验证未完成，请先*私聊*运行 /start 命令 完成身份验证")

    return record
}

fun Auths.updateTgUserInfo(name: String, description: String) {
    var hasUpdate = false

    if (this.tgUserName != name) {
        this.tgUserName = name
        hasUpdate = true
    }

    if (this.tgUserDescription != description) {
        this.tgUserDescription = description
        hasUpdate = true
    }

    if (hasUpdate)
        AuthsDao.update(this)
}

fun AbstractDriver<*>.withPrivateAuthMessage(update: BotUpdate<*>, callback: (auth: Auths) -> Unit) {
    if (update.message != null) {
        try {
            if (update.message!!.messageFrom == MessageFrom.Private) {
                val auth = getAuthInfoByTgId(update.fromUserId.toInt())

                callback(auth)
            }
        } catch (exception: UserViolationException) {
            if (!exception.message.isNullOrEmpty())
                this.botProvider.sendMessage(update, exception.message ?: "")
        }
    }
}
