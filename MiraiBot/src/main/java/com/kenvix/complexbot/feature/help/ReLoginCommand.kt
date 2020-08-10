package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.callBridge
import net.mamoe.mirai.message.MessageEvent

object ReLoginCommand : BotCommandFeature {
    override val description: String
        get() = "重新登录 Mirai"

    override suspend fun onMessage(msg: MessageEvent) {
        msg.reply("正在停止服务并重新启动 Mirai")

        callBridge.driver.miraiComponent!!.restart(200)
    }
}