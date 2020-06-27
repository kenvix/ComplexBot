package com.kenvix.complexbot

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.MessagePacketSubscribersBuilder
import net.mamoe.mirai.message.MessageEvent

const val CommandPrefix = "."
lateinit var commands: HashMap<String, RegisteredBotCommand>


fun MessagePacketSubscribersBuilder.command(command: String,
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
                                if (!middle.onMessage(this@startsWith)) {
                                    success = false
                                    break
                                }
                            }
                        }

                        if (success) this.handler.onMessage(this@startsWith)
                    }
                }
        )
    }

    commands[command] = RegisteredBotCommand(handler, middleware)
}

interface BotCommandFeature {
    suspend fun onMessage(msg: MessageEvent)
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