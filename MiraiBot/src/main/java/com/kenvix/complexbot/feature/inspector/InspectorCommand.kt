package com.kenvix.complexbot.feature.inspector

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.InspectorOptions
import com.kenvix.complexbot.callBridge
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import com.kenvix.moecraftbot.ng.lib.bot.BotCommandQueryData
import com.kenvix.moecraftbot.ng.lib.exception.UserInvalidUsageException
import com.kenvix.utils.log.Logging
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.toMessage

object InspectorCommand : BotCommandFeature, Logging {
    @Throws(UserInvalidUsageException::class, NumberFormatException::class)
    override suspend fun onMessage(msg: MessageEvent) {
        val member = msg.sender as Member
        val group = member.group
        val command = parseCommandFromMessage(msg.message.content, true)
        checkArgumentNum(command, 1)

        val groupOpt = callBridge.getGroupOptions(member.group.id)
        val opt = groupOpt.inspector

        when (command.firstArgument) {
            "help" -> {
                sendHelp(group, opt)
                return
            }

            "list" -> {
                sendList(group, opt)
                return
            }

            "enable" -> {
                opt.enabled = true
                sendMessageAndLog(group, "对群组 ${group.id} 启用 Inspector 成功")
            }

            "disable" -> {
                opt.enabled = false
                sendMessageAndLog(group, "对群组 ${group.id} 停用 Inspector 成功")
            }

            "white" -> {
                checkArgumentNum(command, 2)
                opt.white.add(command.arguments[1].toLong())
                sendMessageAndLog(group, "对用户 ${command.arguments[1]} 在群组 ${group.id} 添加白名单成功")
            }

            "black" -> {
                checkArgumentNum(command, 2)
                opt.white.remove(command.arguments[1].toLong())
                sendMessageAndLog(group, "对用户 ${command.arguments[1]} 在群组 ${group.id} 取消白名单成功")
            }

            "del" -> {
                checkArgumentNum(command, 2)
                val rule = command.arguments[1].toLowerCase()
                opt.rules.remove(rule)
                sendMessageAndLog(group, "删除规则成功：$rule")
            }

            "add" -> {
                checkArgumentNum(command, 3)
                val rule = command.arguments[1].toLowerCase()
                val punishment = command.arguments[2].toLowerCase()
                if (!inspectorRules.containsKey(rule) || !punishments.containsKey(punishment))
                    throw UserInvalidUsageException("失败：规则 $rule 或惩罚 $punishment 不存在")

                opt.rules[rule] = punishment
                sendMessageAndLog(group, "添加规则成功：当满足 $rule 时执行惩罚 $punishment")
            }

            else -> throw UserInvalidUsageException("Inspector 用法错误，非法参数。请输入 .inspector help 查看使用说明")
        }

        InspectorFeature.applyInspectorOptions(group.id, opt)
        callBridge.saveGroupOptions(group.id, groupOpt)
    }

    private suspend fun sendHelp(group: Group, options: InspectorOptions) = withContext(IO) {
        val helpString = StringBuilder()
        helpString.appendln("Inspector: QQ 群广告过滤器和消息监视器. 用法：")
        helpString.appendln("列出本群已启用的规则和白名单用户：.inspector list")
        helpString.appendln("启用或禁用 Inspector：.inspector enable|disable")
        helpString.appendln("添加或删除过滤规则：.inspector add|del 过滤规则名 惩罚规则名")
        helpString.appendln("添加或删除白名单：.inspector white|black 需要加白名单或取消加白名单的QQ号")
        helpString.appendln("示例：添加或修改规则：.inspector add photoqrad kick")
        helpString.appendln("示例：删除规则：.inspector del photoqrad")
        helpString.appendln("示例：添加用户白名单：.inspector white 1234567890")
        helpString.appendln("示例：取消用户白名单：.inspector black 1234567890")
        helpString.appendln("===========================")

        helpString.appendln("已安装下列消息过滤规则：规则名 帮助")
        inspectorRules.forEach { (t, u) -> helpString.appendln("$t  ${u.description}") }
        helpString.appendln("已安装下列惩罚规则：规则名 帮助")
        punishments.forEach { (t, u) -> helpString.appendln("$t  ${u.description}") }

        if (!options.enabled)
            helpString.appendln("注意：本群没有启用此功能，启用请输入：.inspector enable")

        group.sendMessage(helpString.toString().toMessage())
    }

    private suspend fun sendList(group: Group, options: InspectorOptions) = withContext(IO) {
        val helpString = StringBuilder()
        helpString.appendln("Inspector: QQ 群广告过滤器和消息监视器.")

        if (!options.enabled) {
            helpString.appendln("注意：本群没有启用此功能，启用请输入：.inspector enable")
        } else {
            helpString.append("已启用。")
            if (group.botPermission.level >= 1)
                helpString.append("权限正常。")
            else
                helpString.append("但Bot没有管理员权限，无法执行任何惩罚")
        }

        helpString.appendln("===========================")
        helpString.appendln("本群 (${group.id}) 白名单列表：")
        options.white.forEach { helpString.appendln(it) }
        helpString.appendln("===========================")
        helpString.appendln("本群 (${group.id}) 已启用以下过滤规则及惩罚：")
        options.rules.forEach { (t, u) -> helpString.appendln("$t  ${u}") }

        group.sendMessage(helpString.toString())
    }

    private suspend fun sendMessageAndLog(contact: Contact, message: String) {
        logger.info("[${contact.id}] $message")
        contact.sendMessage(message)
    }

    private fun checkArgumentNum(command: BotCommandQueryData, minNum: Int) {
        if (command.arguments.size < minNum)
            throw UserInvalidUsageException("Inspector 用法错误，参数不足。请输入 .inspector help 查看使用说明")
    }
}