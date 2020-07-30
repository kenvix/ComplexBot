//--------------------------------------------------
// Class Command
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib

data class CommandQueryData(val command: String, val arguments: List<String> = listOf()) {
    val firstArgument
        get() = arguments[0]

    val secondArgument
        get() = arguments[1]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandQueryData

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
        return "$command ${arguments.joinToString(" ")}"
    }
}

object ConsoleCommands : HashMap<String, ((CommandQueryData) -> Unit)>() {

    operator fun invoke(command: String) {
        invoke(parseCommandFromMessage(command, true))
    }

    operator fun invoke(command: CommandQueryData) {
        this[command.command]?.invoke(command)
    }

    /**
     * Parse command from a message
     * Make sure a message is a command before invoking!
     * Should be run in thread pool
     */
    fun parseCommandFromMessage(message: String, isMultiCommand: Boolean = false): CommandQueryData {
        val pureMessage = message.trim()
        val commandArray: List<String>
        val command: String

        if (isMultiCommand) {
            commandArray = pureMessage.split(' ')
        } else {
            val spaceIndex = pureMessage.indexOf(' ')
            commandArray = if (spaceIndex == -1) listOf(pureMessage.substring(0), "") else
                listOf(pureMessage.substring(0, spaceIndex), pureMessage.substring(spaceIndex+1).trim())
        }

        command = commandArray[0].toLowerCase()

        return CommandQueryData(
            command,
            if (isMultiCommand) commandArray.asSequence().drop(1).filter { it.length > 1 }.map { it.trim() }.toList()
            else listOf(commandArray[1]))
    }
}