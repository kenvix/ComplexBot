package com.kenvix.complexbot

import com.kenvix.utils.log.Logging
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.SilentLogger

class ComplexBotMiraiComponent(private val callBridge: CallBridge) : AutoCloseable, Logging {
    private val bot: Bot = Bot(
            qq = callBridge.config.bot.qq,
            password = callBridge.config.bot.password
    ) {
        // 覆盖默认的配置
        loginSolver = ExtendedLoginSolver(callBridge)
        networkLoggerSupplier = { SilentLogger } // 禁用网络层输出
    }

    internal fun start() {
        logger.trace("This bot provider is based on mirai project.")
        logger.trace("See miral github: https://github.com/mamoe/mirai")
    }

    override fun close() {

    }
}