package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import org.ahocorasick.trie.Trie

object UselessApp : InspectorRule {
    override val version: Int = 1
    override val description: String = "无用QQ小程序"
    override val punishReason: String = "请勿分享无用QQ小程序"
    override val name: String = "uselessapp"

    val whiteKeywords = "腾讯|中北|学院|文档|收集表|龙山寒泉|大学|金山|知识|微信|高校|服务|企业|通信行程卡|电子票夹" +
            "|哔哩哔哩|bilibili|acfun|AcFun|投票|统计|问卷|打卡|简历|准考证|四六级|查询|表单|工具|报名|签到|班级|管理" +
            "|计算机|计算器"

    val whiteMatchPattern = Trie.builder()
        .addKeywords(whiteKeywords.split('|'))
        .stopOnHit()
        .build()

    override suspend fun onMessage(msg: MessageEvent): Boolean {
        return msg.message.firstIsInstanceOrNull<LightApp>()?.let { appMessage ->
            val content = appMessage.content
            if (content.startsWith('{') && content.endsWith('}')) {
                content.indexOf("\"title\"").let {  beginPos ->
                    !whiteMatchPattern.containsMatch(content.substring(beginPos + 8))
                }
            } else {
                false
            }
        } ?: false
    }
}