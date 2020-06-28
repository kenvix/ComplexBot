package com.kenvix.complexbot

import com.kenvix.moecraftbot.ng.lib.exception.BusinessLogicException
import com.kenvix.moecraftbot.ng.lib.exception.UserInvalidUsageException
import com.kenvix.moecraftbot.ng.lib.exception.UserViolationException
import com.kenvix.moecraftbot.ng.lib.nameAndHashcode
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.MessagePacketSubscribersBuilder
import net.mamoe.mirai.message.MessageEvent
import org.slf4j.LoggerFactory
import java.lang.NumberFormatException

const val CommandPrefix = "."
val enabledFeatures = ArrayList<BotFeature>()
lateinit var commands: HashMap<String, RegisteredBotCommand>
val logger = LoggerFactory.getLogger("ComplexBot.ExtensionUtils")

fun MessagePacketSubscribersBuilder.command(callBridge: CallBridge,
                                            command: String,
                                            handler: BotCommandFeature,
                                            vararg middleware: BotMiddleware
) {
    if (!::commands.isInitialized) {
        commands = HashMap()
        this.startsWith(
                prefix = CommandPrefix,
                trim = true,
                onEvent = {
                    commands[command]?.run {
                        var success = true
                        if (middlewares != null) {
                            for (middle in middlewares) {
                                if (!executeCatchingBusinessException { middle.onMessage(this@startsWith) }) {
                                    success = false
                                    break
                                }
                            }
                        }

                        if (success) {
                            executeCatchingBusinessException {
                                this.handler.onMessage(this@startsWith, callBridge)
                            }
                        }
                    }
                }
        )
    }

    commands[command] = RegisteredBotCommand(handler, middleware)
}

interface BotCommandFeature {
    suspend fun onMessage(msg: MessageEvent, callBridge: CallBridge)
}

interface BotFeature {
    fun onEnable(bot: Bot, callBridge: CallBridge)
}

fun Bot.addFeature(callBridge: CallBridge, handler: BotFeature) {
    enabledFeatures.add(handler)
    handler.onEnable(this, callBridge)
}

suspend fun MessageEvent.executeCatchingBusinessException(function: suspend (() -> Unit)): Boolean {
    return try {
        function()
        true
    } catch (exception: Throwable) {
        when (exception) {
            is UserViolationException, is NumberFormatException -> {
                sendExceptionMessage(exception)
            }

            is BusinessLogicException -> {
                sendExceptionMessage(exception)
                logger.warn("An unhandled business exception is thrown ", exception)
            }

            is NotImplementedError -> {
                logger.warn("An Unimplemented method is called. ", exception)
            }

            else -> throw exception
        }

        false
    }
}

suspend fun MessageEvent.sendExceptionMessage(exception: Throwable) {
    this.sender.sendMessage(kotlin.run {
        if (exception.localizedMessage.isNullOrBlank())
            "操作失败：${exception.nameAndHashcode}"
        else
            exception.localizedMessage
    })
}

interface BotMiddleware {
    suspend fun onMessage(msg: MessageEvent): Boolean
}

data class RegisteredBotCommand(
        val handler: BotCommandFeature,
        val middlewares: Array<out BotMiddleware>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegisteredBotCommand

        if (handler != other.handler) return false
        if (middlewares != null) {
            if (other.middlewares == null) return false
            if (!middlewares.contentEquals(other.middlewares)) return false
        } else if (other.middlewares != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = handler.hashCode()
        result = 31 * result + (middlewares?.contentHashCode() ?: 0)
        return result
    }
}