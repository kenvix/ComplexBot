package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import net.mamoe.mirai.message.MessageEvent

object UselessApp : InspectorRule {
    override val version: Int = 1
    override val description: String = "无用QQ小程序"
    override val punishReason: String = "请勿分享无用QQ小程序"
    override val name: String = "uselessapp"

    override suspend fun onMessage(msg: MessageEvent): Boolean {
        return false
    }
}