package com.kenvix.complexbot.feature.props

import com.kenvix.complexbot.BotCommandFeature
import net.mamoe.mirai.message.MessageEvent

object ListPropCommand : BotCommandFeature {
    override val description: String = "列出所有可用属性"

    override suspend fun onMessage(msg: MessageEvent) {
        TODO("Not yet implemented")
    }
}