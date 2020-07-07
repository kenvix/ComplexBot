package com.kenvix.complexbot.feature.middleware

import com.kenvix.complexbot.BotMiddleware
import com.kenvix.complexbot.callBridge
import com.kenvix.complexbot.isBotSystemAdministrator
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.message.MessageEvent

object AdminPermissionRequired : BotMiddleware {
    override suspend fun onMessage(msg: MessageEvent, command: String?): Boolean {
        if (isBotSystemAdministrator(msg.sender.id))
            return true

        if (msg.sender !is Member)
            return false

        return (msg.sender as Member).isAdministrator()
    }
}