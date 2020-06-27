package com.kenvix.complexbot.feature.middleware

import com.kenvix.complexbot.BotMiddleware
import net.mamoe.mirai.message.MessageEvent

object GroupMessageOnly : BotMiddleware {
    override suspend fun onMessage(msg: MessageEvent): Boolean {
        TODO("Not yet implemented")
    }
}