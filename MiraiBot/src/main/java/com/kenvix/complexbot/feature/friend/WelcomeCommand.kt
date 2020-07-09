package com.kenvix.complexbot.feature.friend

import com.kenvix.complexbot.BotCommandFeature
import net.mamoe.mirai.message.MessageEvent

object WelcomeCommand : BotCommandFeature {
    override val description: String
        get() = "迎新设置"

    override suspend fun onMessage(msg: MessageEvent) {

    }
}