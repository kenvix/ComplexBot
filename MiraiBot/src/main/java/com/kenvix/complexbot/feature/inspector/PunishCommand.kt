package com.kenvix.complexbot.feature.inspector

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.feature.inspector.rule.ManualPunishmentRule
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.*

object PunishCommand : BotCommandFeature {
    override val description: String = "回复某句消息来快速对该消息的发送者执行某个惩罚（仅限管理员）"

    override suspend fun onMessage(msg: MessageEvent) {
        try {
            val quotes = msg.message.filterIsInstance<QuoteReply>()

            if (quotes.isNotEmpty()) {
                val command = parseCommandFromMessage(msg.message.filterIsInstance<PlainText>().joinToString(" "))
                val punish = punishments[command.firstArgumentOrNull?.toLowerCase()] ?: Withdraw

                quotes.forEach {
                    val target = (msg.subject as Group).members[it.source.fromId]

                    punish.punish(
                        msg.subject as Group,
                        target,
                        "Manual Operation",
                        it.source,
                        ManualPunishmentRule
                    )

                    if ((msg.sender as Member).permission.level == 0)
                        msg.message.recall()
                }
            } else {
                msg.reply("命令用法错误：此命令必须回复一条消息。被回复人为被惩罚者\n" +
                        "然后，输入要执行的惩罚名称。惩罚名称参见 .inspector help  （默认为 withdraw）")
            }
        } catch (e: Exception) {
            msg.reply(e.toString())
        }
    }
}