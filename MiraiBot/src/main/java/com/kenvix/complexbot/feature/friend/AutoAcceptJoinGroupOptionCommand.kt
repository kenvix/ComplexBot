package com.kenvix.complexbot.feature.friend

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.InspectorOptions
import com.kenvix.complexbot.callBridge
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import com.kenvix.moecraftbot.ng.lib.bot.BotCommandQueryData
import com.kenvix.moecraftbot.ng.lib.exception.UserInvalidUsageException
import com.kenvix.utils.log.Logging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.content
import java.util.regex.Pattern

object AutoAcceptJoinGroupOptionCommand : BotCommandFeature, Logging {
    override val description: String = "设置自动同意加群请求"

    override suspend fun onMessage(msg: MessageEvent) {
        val member = msg.sender as Member
        val group = member.group
        val command = parseCommandFromMessage(msg.message.content, true)
        checkArgumentNum(command, 1)

        val groupOpt = callBridge.getGroupOptions(member.group.id)
        val opt = groupOpt.options

        when (command.firstArgument) {
            "help" -> {
                sendHelp(group)
                return
            }

            "enable" -> {
                opt[AutoAcceptJoinGroupRequest.EnabledKeyName] = "true"
                sendMessageAndLog(group, "对群组 ${group.id} 启用 AutoAcceptJoinGroup 成功，请输入 .autoaccept set 表达式 来设置正则表达式")
            }

            "disable" -> {
                opt[AutoAcceptJoinGroupRequest.EnabledKeyName] = "false"
                sendMessageAndLog(group, "对群组 ${group.id} 停用 AutoAcceptJoinGroup 成功")
            }

            "set" -> {
                try {
                    val rule = command.arguments.drop(1).joinToString(" ")
                    if (rule.isNotBlank()) {
                        val pa = Pattern.compile(rule)
                        pa.matcher("MATCH SYNTAX CHECK").find()

                        opt[AutoAcceptJoinGroupRequest.MatchRuleName] = rule
                        AutoAcceptJoinGroupRequest.patternCache[group.id] = pa
                        sendMessageAndLog(group, "对群组 ${group.id} 更新规则成功：" + opt[AutoAcceptJoinGroupRequest.MatchRuleName])
                    } else {
                        group.sendMessage("正则规则不可为空")
                    }
                } catch (e: Exception) {
                    group.sendMessage("更新规则失败，请检查正则表达式是否有语法错误：$e")
                }
            }

            "get" -> {
                group.sendMessage(opt[AutoAcceptJoinGroupRequest.MatchRuleName] ?: "[无规则]")
            }

            else -> throw UserInvalidUsageException("AcceptJoinGroup 用法错误，非法参数。请输入 .autoaccept help 查看使用说明")
        }
        
        callBridge.saveGroupOptions(groupOpt)
    }

    private suspend fun sendHelp(group: Group) = withContext(Dispatchers.IO) {
        
    }

    private suspend fun sendMessageAndLog(contact: Contact, message: String) {
        logger.info("[${contact.id}] $message")
        contact.sendMessage(message)
    }

    private fun checkArgumentNum(command: BotCommandQueryData, minNum: Int) {
        if (command.arguments.size < minNum)
            throw UserInvalidUsageException("AcceptJoinGroup 用法错误，参数不足。请输入 .autoaccept help 查看使用说明")
    }
}