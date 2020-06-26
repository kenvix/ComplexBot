//--------------------------------------------------
// Class AbstractDriver
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.mirai.lib.bot

import com.kenvix.moecraftbot.mirai.middleware.BotMiddleware
import com.kenvix.moecraftbot.ng.Defines
import com.kenvix.moecraftbot.ng.lib.BaseFunctionalEntity
import com.kenvix.moecraftbot.ng.lib.bot.BotCommandQueryData
import com.kenvix.moecraftbot.ng.lib.bot.BotInfo
import com.kenvix.moecraftbot.ng.lib.exception.InvalidConfigException
import com.kenvix.moecraftbot.ng.lib.exception.WrongBotCommandTargetException
import com.kenvix.moecraftbot.ng.lib.nameAndHashcode
import com.kenvix.utils.log.warning
import java.util.*

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
        val pureMessage = message.trim()
        val commandArray: List<String>
        val command: String

        if (isMultiCommand) {
            commandArray = pureMessage.split(' ')
        } else {
            val spaceIndex = pureMessage.indexOf(' ')
            commandArray = if (spaceIndex == -1) listOf(pureMessage.substring(1), "") else listOf(pureMessage.substring(1, spaceIndex), pureMessage.substring(spaceIndex+1).trim())
        }

        if (commandArray[0].contains('@')) {
            val commandWithTarget = commandArray[0].split('@')

            if (botName != "" && commandWithTarget[1].toLowerCase() != botNameAsLowerCase)
                throw WrongBotCommandTargetException()

            command = if (isMultiCommand) commandWithTarget[0].substring(1) else commandWithTarget[0]
        } else {
            command = if (isMultiCommand) commandArray[0].substring(1) else commandArray[0]
        }

        return BotCommandQueryData(
            command,
            if (isMultiCommand) commandArray.drop(1).filter { it.length > 1 }.map { it.trim() } else listOf(commandArray[1]))
    }
}