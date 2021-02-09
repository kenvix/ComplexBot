package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import net.mamoe.mirai.event.events.MessageEvent

object DebugActiveDataCommand : BotCommandFeature {
    override val description: String
        get() = "调试 ActiveData"

    override suspend fun onMessage(msg: MessageEvent) {

    }
}