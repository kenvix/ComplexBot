package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.callBridge
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.message.MessageEvent
import org.slf4j.LoggerFactory

object ReLoginCommand : BotCommandFeature {
    override val description: String
        get() = "重新登录 Mirai（仅限核心操作员）"

    private val logger = LoggerFactory.getLogger(this::class.java)

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