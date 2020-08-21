package com.kenvix.complexbot.feature.middleware

import com.kenvix.complexbot.feature.inspector.InspectorRule
import net.mamoe.mirai.message.MessageEvent

object UselessfulSmallPrograms : InspectorRule {
    override val name: String = "uselesssmallprogams"
    override val version: Int = 1
    override val description: String = "低级趣味无用小程序分享"
    override val punishReason: String = "请勿分享低级趣味无用小程序"

    override suspend fun onMessage(msg: MessageEvent): Boolean {
        return false
    }
}