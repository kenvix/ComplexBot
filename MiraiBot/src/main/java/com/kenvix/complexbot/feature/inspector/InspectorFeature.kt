package com.kenvix.complexbot.feature.inspector

import com.kenvix.complexbot.BotFeature
import com.kenvix.complexbot.CallBridge
import com.kenvix.complexbot.command
import com.kenvix.complexbot.feature.middleware.AdminPermissionRequired
import com.kenvix.complexbot.feature.middleware.GroupMessageOnly
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeMessages

object InspectorFeature : BotFeature {
    override fun onEnable(bot: Bot) {
        bot.subscribeMessages {
            command("inspector", InspectorCommand, GroupMessageOnly, AdminPermissionRequired)
        }

        bot.subscribeGroupMessages {
            
        }
    }
}