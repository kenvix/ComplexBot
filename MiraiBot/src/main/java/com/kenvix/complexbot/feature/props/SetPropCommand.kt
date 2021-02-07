package com.kenvix.complexbot.feature.props

import com.kenvix.complexbot.BotCommandFeature
import net.mamoe.mirai.event.events.MessageEvent

object SetPropCommand : BotCommandFeature {
    override val description: String = "设置属性"

    override suspend fun onMessage(msg: MessageEvent) {
        TODO("Not yet implemented")
    }
}