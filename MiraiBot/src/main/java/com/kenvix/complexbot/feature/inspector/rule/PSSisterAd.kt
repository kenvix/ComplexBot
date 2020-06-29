package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import net.mamoe.mirai.message.MessageEvent

object PSSisterAd : InspectorRule {
    override val name: String
        get() = "pssisterad"
    override val version: Int
        get() = 1
    override val description: String
        get() = "PS 学姐广告及黄赌毒广告"
    override val punishReason: String
        get() = "是PS学姐"

    override suspend fun onMessage(msg: MessageEvent): Boolean {
        return false
    }
}