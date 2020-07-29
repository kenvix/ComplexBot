package com.kenvix.complexbot.feature.inspector

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import com.kenvix.moecraftbot.ng.lib.exception.UserInvalidUsageException
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.content

object RankingCommand : BotCommandFeature {
    override val description: String = "本群总体水群排行榜 (可按日期查看)"
    const val MaxOutputNum = 8

    override suspend fun onMessage(msg: MessageEvent) {
        val sender = msg.sender as Member
        val data = InspectorStatisticUtils.getStat(sender.group.id)
        val replyText = StringBuilder("本群 (${sender.group.name})[${sender.group.id}] 水群排行\n")
        val command = parseCommandFromMessage(msg.message.content, false)

        val day = if (command.firstArgumentOrNull.isNullOrBlank()) {
            replyText.append("今日统计信息。添加类似 20200729 格式的参数查询指定日期的统计")
            InspectorStatisticUtils.todayKey
        } else {
            command.firstArgument.also {
                if (it.length != 8)
                    throw UserInvalidUsageException("日期必须为类似 20200729 这样的格式")
                replyText.append("在 $it 这一天的统计信息：")
            }.toInt()
        }

        data.stats.asSequence().filterNot {
            it.value.counts[day] == null
        }.sortedByDescending {
            it.value.counts[day] ?: 0
        }.take(MaxOutputNum).forEachIndexed { index, entry ->
            replyText.append("\n$index. ${entry.value.name}: ${entry.value.counts[day]}")
        }

        msg.reply(replyText.toString())
    }
}