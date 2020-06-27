package com.kenvix.complexbot.feature.inspector

import com.kenvix.complexbot.BotCommandFeature
import net.mamoe.mirai.message.MessageEvent

object InspectorCommand : BotCommandFeature {
    val rules = HashMap<String, InspectorRule>()

    override suspend fun onMessage(msg: MessageEvent) {

    }
}