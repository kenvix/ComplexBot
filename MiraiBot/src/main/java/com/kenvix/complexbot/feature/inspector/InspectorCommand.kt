package com.kenvix.complexbot.feature.inspector

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import com.kenvix.moecraftbot.ng.lib.exception.UserInvalidUsageException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.content

object InspectorCommand : BotCommandFeature {
    override suspend fun onMessage(msg: MessageEvent) {
        val member = msg.sender as Member
        val command = parseCommandFromMessage(msg.message.content, true)
        if (command.arguments.size < 2)
            throw UserInvalidUsageException("Inspector 用法错误，参数不足。请输入 .inspector help 查看使用说明")

        when (command.arguments[0]) {
            "help" -> sendHelp(member.group)
            else -> throw UserInvalidUsageException("Inspector 用法错误，非法参数。请输入 .inspector help 查看使用说明")
        }
    }

    private suspend fun sendHelp(group: Group) = withContext(IO) {
        val helpString = StringBuilder()
        helpString.appendln("Inspector: QQ 群广告过滤器和消息监视器")
        helpString.appendln("用法：.inspector add|del 过滤规则名 惩罚规则名")
        helpString.appendln("用法：.inspector white|black 需要加白名单或取消加白名单的QQ号")
        helpString.appendln("示例：添加或修改规则：.inspector add photoqrad kick")
        helpString.appendln("示例：删除规则：.inspector del photoqrad")
        helpString.appendln("示例：添加用户白名单：.inspector white 1234567890")
        helpString.appendln("示例：取消用户白名单：.inspector black 1234567890")
        helpString.appendln("===========================")
        helpString.appendln("已安装下列消息过滤规则：规则名 帮助")
        inspectorRules.forEach { (t, u) -> helpString.appendln("$t  ${u.description}") }
        helpString.appendln("已安装下列惩罚规则：规则名 帮助")
        punishments.forEach { (t, u) -> helpString.appendln("$t  ${u.description}") }
        helpString.appendln("===========================")
        helpString.appendln("本群 (${group.id}) 已启用以下过滤规则及惩罚：")
        group.sendMessage(helpString.toString())
    }
}