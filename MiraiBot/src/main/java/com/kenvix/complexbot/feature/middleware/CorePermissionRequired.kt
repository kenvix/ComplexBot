package com.kenvix.complexbot.feature.middleware

import com.kenvix.complexbot.BotMiddleware
import com.kenvix.complexbot.isBotSystemAdministrator
import net.mamoe.mirai.event.events.MessageEvent

object CorePermissionRequired : BotMiddleware {
    override suspend fun onMessage(msg: MessageEvent, command: String?): Boolean {
        if (!isBotSystemAdministrator(msg.sender.id)) {
            msg.reply("权限不足：只有核心操作员可以执行此指令。")
            return false
        }

        return true
    }
}