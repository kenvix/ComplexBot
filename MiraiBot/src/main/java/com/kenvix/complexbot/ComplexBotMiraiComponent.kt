package com.kenvix.complexbot

import com.kenvix.android.utils.Coroutines
import com.kenvix.complexbot.feature.featureRoutes
import com.kenvix.utils.log.Logging
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.SilentLogger

class ComplexBotMiraiComponent(
        private val qq: Long,
        private val password: String
) : AutoCloseable, Logging {
    private val bot: Bot = Bot(
            qq = qq,
            password = password
    ) {
        // 覆盖默认的配置
        loginSolver = ExtendedLoginSolver(callBridge)
        // networkLoggerSupplier = { SilentLogger } // 禁用网络层输出
        deviceInfo = { ExtendedDeviceInfo }
    }

    private val coroutines = Coroutines()

    internal fun start() {
        logger.trace("This bot provider is based on mirai project.")
        logger.trace("See miral github: https://github.com/mamoe/mirai")

        coroutines.ioScope.launch {
            bot.login()
            initMirai()
        }
    }

    private suspend fun initMirai() = withContext(IO) {
        bot.featureRoutes()
        logger.info("Mirai Bot setup success: ${bot.nick}(${bot.selfQQ})")
    }

    override fun close() {
        bot.close()
        coroutines.close()
    }
}