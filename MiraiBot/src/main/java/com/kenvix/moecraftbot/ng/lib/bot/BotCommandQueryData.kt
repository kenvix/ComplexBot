//--------------------------------------------------
// Class Command
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.bot

data class BotCommandQueryData(val command: String, val arguments: List<String> = listOf()) {
    val firstArgument
        get() = arguments[0]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BotCommandQueryData

        if (command != other.command) return false
        if (arguments != other.arguments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = command.hashCode()
        result = 31 * result + arguments.hashCode()
        return result
    }

    override fun toString(): String {
        return "/$command ${arguments.joinToString(" ")}"
    }
}