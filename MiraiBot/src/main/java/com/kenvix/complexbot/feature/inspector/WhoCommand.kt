package com.kenvix.complexbot.feature.inspector

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import com.kenvix.moecraftbot.ng.lib.format
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText

object WhoCommand : BotCommandFeature {
    override val description: String = "获取某人的加群信息和发言日志（后面跟要查询的QQ号或At某人，例如.who 1145141919）"

    override suspend fun onMessage(msg: MessageEvent) {
        sendWhoIsMsg((msg.sender as Member).group.id, msg)
    }

    private suspend fun sendWhoIsMsg(group: Long, msg: MessageEvent) {
        msg.message.asSequence().also { msgSeq ->
            val stat = InspectorStatisticUtils.getStat(group)

            (msgSeq.filterIsInstance<At>().map {
                it.target
            } + msgSeq.filterIsInstance<PlainText>().joinToString(" ").run {
                parseCommandFromMessage(this, true).arguments
            }.mapNotNull {
                it.toLongOrNull()
            }).ifEmpty {
                sequenceOf(msg.sender.id)
            }.run {
                val todayKey = InspectorStatisticUtils.todayKey

                forEach { qq ->
                    StringBuilder().apply {
                        val speakStat = stat.stats[qq]
                        val joinStat = stat.joins[qq]

                        append("查询目标：$qq ")

                        if (speakStat == null && joinStat == null) {
                            append("不存在或没有关于此人的统计信息")
                        } else {
                            append(": ")
                            appendLine(speakStat?.name ?: joinStat?.name)

                            if (speakStat != null) {
                                append("发言统计起始：")
                                appendLine(speakStat.createdAt.format())

                                appendLine("总发言数：${speakStat.countLegal} + ${speakStat.countIllegal} = ${speakStat.countTotal}")
                                appendLine("天数：${speakStat.counts.count()} | 今日发言：${speakStat.counts[todayKey] ?: 0}")
                            } else {
                                append("无发言信息统计")
                            }

                            if (joinStat != null) {
                                append("申请加群时间：")
                                appendLine(joinStat.requestedAt.format())

                                if (joinStat.handledAt != null) {
                                    append("审批时间：")
                                    appendLine(joinStat.handledAt!!.format())
                                }

                                append("状态：")
                                append(JoinStatus.valueOf(joinStat.status))
                                appendLine(" <${JoinStatus.valueOf(joinStat.status)}>")

                                append("加群备注：")
                                appendLine(JoinStatus.valueOf(joinStat.note))
                            } else {
                                appendLine("无加群信息记录")
                            }
                        }
                    }.also { msg.reply(it.toString()) }
                }
            }
        }
    }
}