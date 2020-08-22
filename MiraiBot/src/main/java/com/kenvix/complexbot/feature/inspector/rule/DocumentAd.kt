package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import com.kenvix.complexbot.feature.inspector.InspectorStatisticUtils
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import org.ahocorasick.trie.Trie

object DocumentAd : InspectorRule {
    override val name: String = "documentad"
    override val version: Int = 1
    override val description: String = "基于腾讯文档的盗号诈骗消息"
    override val punishReason: String = "疑似被盗号并利用腾讯文档发送了诈骗消息，请勿相信"

    val blackKeywords = "教务处|通知|正式|成绩|最新|学院|管理|兼职|招聘|刷单|有意者|代购|扣扣|客服|微店|兼职|兼值|淘宝|贷款"
    val requiredKeywords = "腾讯文档|在线文档"

    val blackMatchPattern = Trie.builder()
        .addKeywords(blackKeywords.split('|'))
        .stopOnHit()
        .build()

    val requiredMatchPattern = Trie.builder()
        .addKeywords(requiredKeywords.split('|'))
        .onlyWholeWords()
        .stopOnHit()
        .build()

    override suspend fun onMessage(msg: MessageEvent): Boolean {
        return msg.message.firstIsInstanceOrNull<LightApp>()?.let { appMessage ->
            val content = appMessage.content
            if (content.startsWith('{') && content.endsWith('}')) {
                content.indexOf("\"title\"").let {  beginPos ->
                    val sender = msg.sender as Member
                    val stat = InspectorStatisticUtils.getStat(sender.group.id).stats[sender.id]

                    if (beginPos > 0 && requiredMatchPattern.containsMatch(content) &&
                        (stat == null || stat.countLegal <= 30L ||
                                stat.countIllegal >= 4L && stat.counts[InspectorStatisticUtils.todayKey] ?: 0 <= 5)
                    ) {
                        blackMatchPattern.containsMatch(content.substring(beginPos + 8))
                    } else {
                        false
                    }
                }
            } else {
                false
            }
        } ?: false
    }
}