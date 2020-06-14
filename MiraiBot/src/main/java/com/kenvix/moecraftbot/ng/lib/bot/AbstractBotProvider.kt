//--------------------------------------------------
// Class AbstractBotProvider
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.bot

import com.kenvix.moecraftbot.ng.Defines
import com.kenvix.moecraftbot.ng.lib.BaseFunctionalEntity
import com.kenvix.moecraftbot.ng.lib.exception.InvalidConfigException
import com.kenvix.utils.log.info

/**
 * The AbstractBotProvider is the base of all Bot Providers.
 *
 * @author Kenvix
 */
abstract class AbstractBotProvider<T : Any> : BaseFunctionalEntity<T>(), BotInfo, Defines.ConsoleReadSupported {
    abstract val providerName: String
    abstract val providerVersion: String
    abstract val providerVersionCode: Int
    abstract val providerOptions: Int

    val driver = Defines.activeDriver

    private val botNameAsLowerCase: String by lazy(LazyThreadSafetyMode.NONE) { botName.toLowerCase() }

    fun onLoad() {
        logger.fine("Loading bot provider")
        loadConfig()
    }
    /**
     * On enable bot provider
     *
     * Your bot should be initialized in this method, this method will be invoked when driver partially initialized.
     * Therefore, you **must** call {@link AbstractDriver#onBotProviderConnect()} at the right time to finish the initialization of the driver,
     * otherwise, there will be some *unpredictable behavior*
     *
     * @see AbstractDriver
     */
    abstract fun onEnable()
    open fun onDisable(): Unit = logger.fine("Disabling bot provider")

    fun sendMessage(update: BotUpdate<*>, message: String, replyToMessageId: Long? = null)
            = sendMessage(update.chatId, message, update.message!!.messageType, replyToMessageId, update.message!!.messageFrom)

    fun sendMessageNoException(update: BotUpdate<*>, message: String): Boolean {
        return try {
            sendMessage(update, message)
            true
        } catch (e: Exception) {
            logger.info(e, "Send Alternative message failed: ${e.message}")
            false
        }
    }
    abstract fun sendMessage(chatId: Long, message: String, type: MessageType, replyToMessageId: Long? = null,
                             messageFrom: MessageFrom = MessageFrom.Unknown, extraData: BotExtraData? = null): BotMessage

    open fun kickUser(chatId: Long, userId: Long, messageFrom: MessageFrom = MessageFrom.Unknown) {}
    /**
     * Kick and ban user
     * @param duration How long to ban. If equal or less than -1 will unban, otherwise depends on API implementation
     */
    open fun banUser(chatId: Long, userId: Long, duration: Int = -1, messageFrom: MessageFrom = MessageFrom.Unknown) {}
    fun banUser(update: BotUpdate<*>, duration: Int = -1, messageFrom: MessageFrom = MessageFrom.Unknown)
            = banUser(update.chatId, update.fromUserId, duration, messageFrom)
    fun unbanUser(update: BotUpdate<*>)
            = banUser(update.chatId, update.fromUserId, -1, update.message?.messageFrom ?:MessageFrom.Unknown)
    fun unbanUser(chatId: Long, userId: Long, messageFrom: MessageFrom = MessageFrom.Unknown)
            = banUser(chatId, userId, -1, messageFrom)
    /**
     * Mute user
     * @param duration How long to ban. If equal or less than -1 will unmute, otherwise depends on API implementation
     */
    open fun muteUser(chatId: Long, userId: Long, duration: Int = 0, messageFrom: MessageFrom = MessageFrom.Unknown) {}
    open fun deleteMessage(chatId: Long, messageId: Long, messageFrom: MessageFrom = MessageFrom.Unknown) {}
    open fun onMessage(update: BotUpdate<*>, messageText: String): Boolean = driver.onMessage(update, messageText)
    open fun onEvent(update: BotUpdate<*>, eventType: MessageType) = driver.onEvent(update, eventType)
    open fun onCommand(update: BotUpdate<*>, commandText: String) = driver.onCommand(update, commandText)

    open fun onInstall() {}
    open fun onUpgrade(newVersionCode: Int, oldVersionCode: Int) {}

    /**
     * On system console input
     * @param input command
     * @return Should stop deliver command to next handler
     */
    override open fun onSystemConsoleInput(input: String): Boolean { return false }

    fun throwInvalidConfigException(): Nothing = throw InvalidConfigException(
        configFileName
    )
    fun isCommandMessage(message: String) = message[0] == '/'

    companion object Info {
        const val OPTION_NONE: Int = 0
        const val OPTION_REDIRECT_STDIN: Int = 0b00000000000000000000010
    }
}