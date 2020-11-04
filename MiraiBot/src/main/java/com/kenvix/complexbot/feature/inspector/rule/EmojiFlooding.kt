package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import com.kenvix.complexbot.feature.inspector.InspectorStatisticUtils
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.message.data.PlainText
import kotlin.math.max

object EmojiFlooding : InspectorRule.Actual {
    override suspend fun onMessage(
        msg: MessageEvent,
        relatedPlaceholders: List<InspectorRule.Placeholder>
    ): InspectorRule? {
        val qqFaceNum = msg.message.asSequence().filterIsInstance<Face>().count()

        if (qqFaceNum >= 7) {
            val sender = msg.sender as Member
            val stat = InspectorStatisticUtils.getStat(sender.group.id).stats[sender.id]
            val text = msg.message.asSequence().filterIsInstance<PlainText>().map {
                it.content
            }.joinToString()

            val today = InspectorStatisticUtils.todayKey
            if (stat == null || stat.countLegal <= 10L ||
                (stat.countIllegal >= 4L && stat.counts[today] ?: 0 <= 5) ||
                (stat.counts[today] ?: 0 <= 1)
            ) {
                if (BayesBasedAd.requiredMatchPattern.containsMatch(text) ||
                    (stat!!.counts[today] ?: 0 <= max(qqFaceNum / 4, 3) && text.length.toDouble() / qqFaceNum.toDouble() <= qqFaceNum)
                ) {
                    return EmojiFlooding
                }
            } else {
                return null
            }
        }
        return null
    }

    override val version: Int = 1
    override val description: String = "大量刷表情的违法广告"
    override val punishReason: String = "大量表情的违法广告"
    override val name: String = "emojiflooding"
}