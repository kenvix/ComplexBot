//--------------------------------------------------
// Class AbstractDriver
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.mirai.lib.bot

import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import com.kenvix.moecraftbot.mirai.middleware.BotMiddleware
import com.kenvix.moecraftbot.ng.Defines
import com.kenvix.moecraftbot.ng.lib.BaseFunctionalEntity
import com.kenvix.moecraftbot.ng.lib.bot.BotCommandQueryData
import com.kenvix.moecraftbot.ng.lib.bot.BotInfo
import com.kenvix.moecraftbot.ng.lib.exception.InvalidConfigException
import com.kenvix.moecraftbot.ng.lib.exception.WrongBotCommandTargetException

/**
 * The AbstractBotProvider is the base of all Drivers.
 *
 * Life cycle of driver: Enable -> BotProviderConnect -> Disable
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractDriver<T : Any> : BaseFunctionalEntity<T>(), BotInfo, Defines.ConsoleReadSupported {
    abstract val driverName: String
    abstract val driverVersion: String
    abstract val driverVersionCode: Int

    var isInitialized = false
        private set

    final override lateinit var botName: String
        private set

    private val botNameAsLowerCase: String by lazy(LazyThreadSafetyMode.NONE) { botName.toLowerCase() }

    /**
     * On enable driver
     *
     * You **shouldn't** call bot provider or initialize middleware here
     */
    open fun onEnable(): Unit {
        logger.info("Enabling driver")
        loadConfig()
    }

    open fun onDisable(): Unit = logger.info("Disabling driver")

    open fun onInstall() {}
    open fun onUpgrade(newVersionCode: Int, oldVersionCode: Int) {}

    /**
     * On system console input
     * @param input command
     * @return Should stop deliver command to next handler
     */
    open override fun onSystemConsoleInput(input: String): Boolean { return false }

    fun throwInvalidConfigException(): Nothing = throw InvalidConfigException(
        configFileName
    )

    fun <R1 : BotMiddleware> enableMiddleware(middleware: R1) : R1 {
        middleware.onEnable(this)
        return middleware
    }

    /**
     * Parse command from a message
     * Make sure a message is a command before invoking!
     * Should be run in thread pool
     */
    @Throws(WrongBotCommandTargetException::class)
    fun parseCommandFromMessage(message: String, isMultiCommand: Boolean = false): BotCommandQueryData {
        return parseCommandFromMessage(message, isMultiCommand, botNameAsLowerCase)
    }
}