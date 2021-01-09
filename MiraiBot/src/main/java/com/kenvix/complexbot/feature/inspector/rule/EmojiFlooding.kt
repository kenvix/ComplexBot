package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import com.kenvix.complexbot.feature.inspector.InspectorStatisticUtils
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.message.data.PlainText
import java.util.regex.Pattern

object EmojiFlooding : InspectorRule.Actual {
    val emojiPattern = Pattern.compile("(\\ud83c[\\udf00-\\udfff])|(\\ud83d[\\udc00-\\ude4f\\ude80-\\udeff])|[\\u2600-\\u2B55]")

    override suspend fun onMessage(
        msg: MessageEvent,
        relatedPlaceholders: List<InspectorRule.Placeholder>
    ): InspectorRule? {
        val sender = msg.sender as Member
        val stat = InspectorStatisticUtils.getStat(sender.group.id).stats[sender.id]
        val today = InspectorStatisticUtils.todayKey

        if (stat == null || stat.countLegal <= 10L ||
            (stat.countIllegal >= 4L && stat.counts[today] ?: 0 <= 5) ||
            (stat.counts[today] ?: 0 <= 1)
        ) {
            val qqFaceNum = msg.message.asSequence().filterIsInstance<Face>().count()
            val text = msg.message.asSequence().filterIsInstance<PlainText>().map {
                it.content
            }.joinToString()

            val emojiMatcher = emojiPattern.matcher(text)
            val emojiNum = emojiMatcher.results().count()
            val totalFaceNum = qqFaceNum + emojiNum

            if (totalFaceNum >= 9) {
                if (BayesBasedAd.requiredMatchPattern.containsMatch(text) ||
                    (stat!!.counts[today] ?: 0 <= 1 && text.length.toDouble() / totalFaceNum.toDouble() <= totalFaceNum)
                ) {
                    return EmojiFlooding
                }
            }
        }
        return null
    }

    override val version: Int = 1
    override val description: String = "大量刷表情的违法广告"
    override val punishReason: String = "大量表情的违法广告"
    override val name: String = "emojiflooding"
}