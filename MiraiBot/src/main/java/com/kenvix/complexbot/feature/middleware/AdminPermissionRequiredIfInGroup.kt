package com.kenvix.complexbot.feature.middleware

import com.kenvix.complexbot.BotMiddleware
import net.mamoe.mirai.message.MessageEvent

object AdminPermissionRequiredIfInGroup : BotMiddleware {
    override suspend fun onMessage(msg: MessageEvent): Boolean {
        TODO("Not yet implemented")
    }
}