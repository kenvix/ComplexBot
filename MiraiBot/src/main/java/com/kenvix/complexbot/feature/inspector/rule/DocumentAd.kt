package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import net.mamoe.mirai.message.MessageEvent

object DocumentAd : InspectorRule {
    override val name: String
        get() = "documentad"
    override val version: Int
        get() = 1
    override val description: String
        get() = "基于腾讯文档的盗号诈骗消息"

    override suspend fun onMessage(msg: MessageEvent): Boolean {
        TODO("Not yet implemented")
    }
}