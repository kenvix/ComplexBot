//--------------------------------------------------
// Class AbstractDriver
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.bot

import com.kenvix.moecraftbot.ng.Defines
import com.kenvix.moecraftbot.ng.lib.BaseFunctionalEntity
import com.kenvix.moecraftbot.ng.lib.BotCommandCallback
import com.kenvix.moecraftbot.ng.lib.exception.InvalidConfigException
import com.kenvix.moecraftbot.ng.lib.exception.WrongBotCommandTargetException
import com.kenvix.moecraftbot.ng.lib.middleware.BotMiddleware
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
    abstract val driverFeatures: EnumSet<DriverFeature>

    var isInitialized = false
        private set

    private var supportedCommands: MutableMap<String, MutableList<BotCommandCallback>> = mutableMapOf()

    lateinit var botProvider: AbstractBotProvider<*>
        private set

    final override lateinit var botName: String
        private set

    private val botNameAsLowerCase: String by lazy(LazyThreadSafetyMode.NONE) { botName.toLowerCase() }
    /**
     * Chat Sessions.
     * TODO: Soft reference.
     * Key: ChatID XOR UserID
     * Value: Chat Session
     */
    private val chatSessions: MutableMap<Long, ChatSession> = mutableMapOf()

    /**
     * On enable driver
     *
     * You **shouldn't** call bot provider or initialize middleware here
     */
    open fun onEnable(): Unit {
        logger.fine("Enabling driver")
        loadConfig()
    }

    /**
     * On Bot Provider Connect
     */
    open fun onBotProviderConnect(botProvider: AbstractBotProvider<*>) {
        this.botProvider = botProvider
        this.botName = botProvider.botName
        logger.fine("Bot provider ${botProvider.providerName} is connecting to ${this.driverName} Driver")
        isInitialized = true
    }

    open fun onDisable(): Unit = logger.fine("Disabling driver")

    open fun onInstall() {}
    open fun onUpgrade(newVersionCode: Int, oldVersionCode: Int) {}

    /**
     * On message
     *
     * @return bool Should intercept message.
     */
    open fun onMessage(update: BotUpdate<*>, message: String): Boolean = false

    open fun onEvent(update: BotUpdate<*>, eventType: MessageType) {
        logger.finest("Chat ${update.chatId} (${update.message!!.sender}) Event: $eventType, ${update.message!!.extraData}")
    }

    open fun onCommand(update: BotUpdate<*>, commandText: String) {
        logger.finest("Chat ${update.chatId} (${update.message!!.sender}) issued command: $commandText")
        if (!isFeatureSupported(DriverFeature.ManualCommandHandleOnly)) {
            try {
                val query: BotCommandQueryData = parseCommandFromMessage(commandText)

                if (supportedCommands.containsKey(query.command))
                    supportedCommands[query.command]?.forEach { it.invoke(update, query) }
            } catch (e: Exception) {
                logger.warning(e, "Driver failed to run command: ${e.message}")
                botProvider.sendMessageNoException(update, "执行命令失败：${e.nameAndHashcode}")
            }
        }
    }

    /**
     * On system console input
     * @param input command
     * @return Should stop deliver command to next handler
     */
    override open fun onSystemConsoleInput(input: String): Boolean { return false }

    fun registerCommand(key: String, callback: BotCommandCallback) {
        if (!supportedCommands.containsKey(key))
            supportedCommands[key] = LinkedList()

        supportedCommands[key]!!.add(callback)
    }

    fun unregisterCommand(key: String, callback: BotCommandCallback) {
        if (supportedCommands.containsKey(key))
            supportedCommands[key]?.remove(callback)
    }

    fun unregisterAllCommandByKey(key: String) {
        if (supportedCommands.containsKey(key))
            supportedCommands.remove(key)
    }

    fun unregisterAllCommand() {
        supportedCommands.clear()
    }

    fun registerCommandGroup(commands: Map<String, BotCommandCallback>) {
        commands.forEach(::registerCommand)
    }

    fun unregisterCommandGroup(commands: Map<String, BotCommandCallback>) {
        commands.forEach(::unregisterCommand)
    }

    fun isFeatureSupported(feature: DriverFeature) = driverFeatures.contains(feature)
    fun isAnyFeatureSupported(vararg features: DriverFeature): Boolean {
        for (feature in features)
            if (driverFeatures.contains(feature))
                return true

        return false
    }

    fun isAllFeatureSupported(vararg features: DriverFeature): Boolean {
        for (feature in features)
            if (!driverFeatures.contains(feature))
                return false

        return true
    }

    fun throwInvalidConfigException(): Nothing = throw InvalidConfigException(
        configFileName
    )

    fun getChatSession(userId: Long, chatId: Long): ChatSession? = chatSessions[userId xor chatId]
    fun containsChatSession(userId: Long, chatId: Long): Boolean = chatSessions.containsKey(userId xor chatId)
    fun removeChatSession(userId: Long, chatId: Long): ChatSession? = chatSessions.remove(userId xor chatId)
    fun getChatSession(userId: Long, chatId: Long, default: ChatSession): ChatSession {
        val key = userId xor chatId

        @Suppress("LiftReturnOrAssignment")
        if (chatSessions.containsKey(key)) {
            return chatSessions[key]!!
        } else {
            chatSessions[key] = default
            return default
        }
    }
    fun setChatSession(userId: Long, chatId: Long, session: ChatSession) {
        chatSessions[userId xor chatId] = session
    }

    fun getChatSession(update: BotUpdate<*>): ChatSession? = getChatSession(update.fromUserId, update.chatId)
    fun containsChatSession(update: BotUpdate<*>): Boolean = containsChatSession(update.fromUserId, update.chatId)
    fun removeChatSession(update: BotUpdate<*>): ChatSession? = removeChatSession(update.fromUserId, update.chatId)
    fun getChatSession(update: BotUpdate<*>, default: ChatSession): ChatSession = getChatSession(update.fromUserId, update.chatId, default)
    fun setChatSession(update: BotUpdate<*>, session: ChatSession): Unit = setChatSession(update.fromUserId, update.chatId, session)

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