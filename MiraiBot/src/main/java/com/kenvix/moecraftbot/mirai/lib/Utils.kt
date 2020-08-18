package com.kenvix.moecraftbot.mirai.lib

import com.kenvix.complexbot.commandPrefixLength
import com.kenvix.moecraftbot.ng.lib.bot.BotCommandQueryData
import com.kenvix.moecraftbot.ng.lib.exception.UserInvalidUsageException
import com.kenvix.moecraftbot.ng.lib.exception.UserViolationException
import com.kenvix.moecraftbot.ng.lib.exception.WrongBotCommandTargetException

/**
 * Parse command from a message
 * Make sure a message is a command before invoking!
 * Should be run in thread pool
 */
@Throws(WrongBotCommandTargetException::class)
fun parseCommandFromMessage(message: String, isMultiCommand: Boolean = false, botName: String? = null): BotCommandQueryData {
    val pureMessage = message.trim()
    val commandArray: List<String>
    val command: String

    if (pureMessage.length < commandPrefixLength)
        throw UserInvalidUsageException("Command length should not be less than $commandPrefixLength: $pureMessage")

    if (isMultiCommand) {
        commandArray = pureMessage.split(' ')
    } else {
        val spaceIndex = pureMessage.indexOf(' ')
        commandArray = if (spaceIndex == -1) listOf(pureMessage.substring(commandPrefixLength), "") else listOf(pureMessage.substring(1, spaceIndex), pureMessage.substring(spaceIndex+1).trim())
    }

    if (commandArray[0].contains('@')) {
        val commandWithTarget = commandArray[0].split('@')

        if (!botName.isNullOrEmpty() && commandWithTarget[1].toLowerCase() != botName)
            throw WrongBotCommandTargetException()

        command = if (isMultiCommand) commandWithTarget[0].substring(commandPrefixLength) else commandWithTarget[0]
    } else {
        command = if (isMultiCommand) commandArray[0].substring(commandPrefixLength) else commandArray[0]
    }

    return BotCommandQueryData(
            command,
            if (isMultiCommand) commandArray.drop(1).filter { it.isNotEmpty() }.map { it.trim() } else listOf(commandArray[1])
    )
}