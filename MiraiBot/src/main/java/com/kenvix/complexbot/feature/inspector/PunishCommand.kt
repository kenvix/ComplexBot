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
            val ats = msg.message.filterIsInstance<At>()
            val group = (msg.subject as Group)

            if (quotes.isNotEmpty()) {
                val punish = getPunishment(msg)

                quotes.forEach {
                    val target = group.members[it.source.fromId]

                    punish.punish(
                        group,
                        target,
                        "管理员手动操作 [Reply]",
                        it.source,
                        ManualPunishmentRule
                    )

                    if ((msg.sender as Member).permission.level == 0 && group.botPermission.level >= 1)
                        msg.message.runCatching { recall() }
                }
            } else if (ats.isNotEmpty()) {
                val punish = getPunishment(msg)

                ats.forEach {
                    val target = group.members[it.target]

                    punish.punish(
                        group,
                        target,
                        "管理员手动操作 [At]",
                        null,
                        ManualPunishmentRule
                    )

                    if ((msg.sender as Member).permission.level == 0 && group.botPermission.level >= 1)
                        msg.message.runCatching { recall() }
                }
            } else {
                msg.reply("命令用法错误：此命令必须回复一条消息。被回复人为被惩罚者\n" +
                        "然后，输入要执行的惩罚名称。惩罚名称参见 .inspector help  （默认为 withdraw）")
            }
        } catch (e: Exception) {
            msg.reply(e.toString())
        }
    }

    private fun getPunishment(msg: MessageEvent): AbstractPunishment {
        val command = parseCommandFromMessage(msg.message.filterIsInstance<PlainText>().joinToString(" "))
        return punishments[command.firstArgumentOrNull?.toLowerCase()] ?: Withdraw
    }
}