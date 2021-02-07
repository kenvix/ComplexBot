package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.RichMessage
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import org.ahocorasick.trie.Trie

object UselessApp : InspectorRule.Actual {
    override val version: Int = 1
    override val description: String = "无用QQ小程序"
    override val punishReason: String = "请勿分享无用QQ小程序"
    override val name: String = "uselessapp"

    private const val whiteKeywords = "腾讯|中北|学院|文档|收集表|龙山寒泉|大学|金山|知识|微信|高校|服务|企业|通信行程卡|电子票夹" +
            "|哔哩哔哩|bilibili|acfun|AcFun|投票|统计|问卷|打卡|简历|准考证|四六级|查询|表单|工具|报名|签到|班级|管理" +
            "|计算机|计算器|优酷|百度|搜狗|浏览器|Via|Chrome|知乎|CSDN|简书|掘金|github|新浪|央视|网易|教程|阿里|大赛"

    private const val searchBeginKeywords = "[QQ小程序]|[小程序]"

    private val whiteMatchPattern: Trie = Trie.builder()
        .addKeywords(whiteKeywords.split('|'))
        .stopOnHit()
        .build()

    private val searchBeginPattern: Trie = Trie.builder()
        .addKeywords(searchBeginKeywords.split('|'))
        .stopOnHit()
        .build()

    override suspend fun onMessage(msg: MessageEvent, relatedPlaceholders: List<InspectorRule.Placeholder>): InspectorRule? {
        return if (
            msg.message.firstIsInstanceOrNull<RichMessage>()?.let { appMessage ->
                val content = appMessage.content.trim()
                if (searchBeginPattern.containsMatch(content)) {
                    content.indexOf("title").let {  beginPos ->
                        if (beginPos > 0)
                            !whiteMatchPattern.containsMatch(content.substring(beginPos, beginPos.run {
                                if (content.length > beginPos + 16) beginPos + 16 else content.length
                            }))
                        else
                            false
                    }
                } else {
                    false
                }
            } == true
        ) this else null
    }
}