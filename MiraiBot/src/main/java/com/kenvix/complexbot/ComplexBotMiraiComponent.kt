package com.kenvix.complexbot

import com.kenvix.android.utils.Coroutines
import com.kenvix.complexbot.feature.featureRoutes
import com.kenvix.moecraftbot.ng.lib.error
import com.kenvix.utils.log.Logging
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.network.WrongPasswordException
import net.mamoe.mirai.utils.BotConfiguration
import kotlin.coroutines.CoroutineContext

class ComplexBotMiraiComponent(
        private val qq: Long,
        private val password: String
) : AutoCloseable, Logging {
    private val coroutines = Coroutines()

    lateinit var bot: Bot
        private set

    private fun getNewBotInstance() = BotFactory.newBot(
        qq = qq,
        password = password
    ) {
        // 覆盖默认的配置
        loginSolver = ExtendedLoginSolver(callBridge)
        // networkLoggerSupplier = { SilentLogger } // 禁用网络层输出
        deviceInfo = { newExtendedDeviceInfo() }
        protocol = when (callBridge.config.mirai.protocol.toLowerCase()) {
            "phone" -> BotConfiguration.MiraiProtocol.ANDROID_PHONE
            "pad" -> BotConfiguration.MiraiProtocol.ANDROID_PAD
            "watch" -> BotConfiguration.MiraiProtocol.ANDROID_WATCH
            else -> {
                logger.warn("Unrecognized config mirai.protocol: ${callBridge.config.mirai.protocol ?: "NULL"} using default 'pad'")
                BotConfiguration.MiraiProtocol.ANDROID_PAD
            }
        }
        logger.debug("Using mirai qq protocol $protocol")

        parentCoroutineContext = coroutines.ioScope.coroutineContext +
                SupervisorJob() +
                CoroutineName("Mirai") +
                CoroutineExceptionHandler(::onException)
    }

    internal fun start() {
        synchronized(this) {
            logger.trace("This bot provider is based on mirai project.")
            logger.trace("See miral github: https://github.com/mamoe/mirai")
            bot = getNewBotInstance()

            coroutines.ioScope.launch {
                bot.login()
                initMirai()
            }
        }
    }

    private suspend fun initMirai() = withContext(IO) {
        bot.featureRoutes()
        logger.info("Mirai Bot setup success: ${bot.nick}(${bot.id})")
    }

    private fun onException(coroutineContext: CoroutineContext, exception: Throwable) {
        error("Mirai Exception on coroutineContext $coroutineContext", exception, logger)

        if (exception is WrongPasswordException) {
            logger.warn("Mirai bot failed due to a WrongPasswordException, will restart it after 10s")
            restart(10_000)
        }
    }

    fun restart(restartDelayTime: Long = 0) {
        coroutines.ioScope.launch {
            restartAndJoin(restartDelayTime)
        }
    }

    suspend fun restartAndJoin(restartDelayTime: Long = 0) {
        if (this::bot.isInitialized)
            kotlin.runCatching { bot.closeAndJoin() }.onFailure { logger.debug("bot close failed", it) }

        if (restartDelayTime > 0)
            delay(restartDelayTime)

        logger.debug("Starting bot ...")
        start()
    }

    override fun close() {
        synchronized(this) {
            bot.close()
            coroutines.close()
        }
    }
}