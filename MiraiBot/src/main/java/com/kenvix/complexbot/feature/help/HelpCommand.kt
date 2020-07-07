package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.CallBridge
import net.mamoe.mirai.message.MessageEvent

object HelpCommand : BotCommandFeature {
    override val description: String
        get() = "帮助"

    override suspend fun onMessage(msg: MessageEvent) {
        val text = StringBuilder("MoeNet Complex Bot v0.1")

        msg.reply(text.toString())
    }
}