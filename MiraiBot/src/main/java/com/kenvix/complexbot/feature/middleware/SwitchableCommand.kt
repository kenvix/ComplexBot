package com.kenvix.complexbot.feature.middleware

import com.kenvix.complexbot.BotMiddleware
import com.kenvix.complexbot.callBridge
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent

object SwitchableCommand : BotMiddleware {
    override suspend fun onMessage(msg: MessageEvent, command: String?): Boolean {
        if (command != null && msg.sender is Member) {
            val sender = msg.sender as Member
            return !callBridge.getGroupOptions(sender.group.id).disabledCommands.contains(command)
        }

        return true
    }
}