package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.callBridge
import com.kenvix.complexbot.logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.message.MessageEvent

object ReLoginCommand : BotCommandFeature {
    override val description: String
        get() = "重新登录 Mirai"

    override suspend fun onMessage(msg: MessageEvent) {
        logger.info("正在停止服务并重新启动 Mirai")
        msg.reply("正在停止服务并重新启动 Mirai")

        GlobalScope.launch {
            callBridge.driver.miraiComponent!!.also {
                it.restartAndJoin(200)
                msg.reply("重新启动成功。")
            }
        }
    }
}