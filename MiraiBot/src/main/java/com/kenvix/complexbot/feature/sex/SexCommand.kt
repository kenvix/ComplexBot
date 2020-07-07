package com.kenvix.complexbot.feature.sex

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.CallBridge
import net.mamoe.mirai.message.MessageEvent

object SexCommand : BotCommandFeature {
    override val description: String
        get() = "文爱"

    override suspend fun onMessage(msg: MessageEvent) {
        TODO("Not yet implemented")
    }
}