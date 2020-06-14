package com.kenvix.moecraftbot.ng.lib.middleware.help

import com.kenvix.moecraftbot.ng.BuildConfig
import com.kenvix.moecraftbot.ng.Defines
import com.kenvix.moecraftbot.ng.lib.bot.AbstractDriver
import com.kenvix.moecraftbot.ng.lib.bot.BotCommandQueryData
import com.kenvix.moecraftbot.ng.lib.bot.BotUpdate
import com.kenvix.moecraftbot.ng.lib.format
import com.kenvix.moecraftbot.ng.lib.middleware.BotMiddleware
import com.kenvix.moecraftbot.ng.lib.replacePlaceholders

class BotHelp : BotMiddleware {
    private lateinit var driverContext: AbstractDriver<*>
    private val helpText: String by lazy {
        Defines.systemOptions.bot.helpMessage.replacePlaceholders(mapOf(
            "botName" to driverContext.botName,
            "version" to BuildConfig.VERSION_NAME,
            "driverName" to driverContext.driverName,
            "driverVersion" to driverContext.driverVersion,
            "botProviderName" to driverContext.botProvider.providerName,
            "botProviderVersion" to driverContext.botProvider.providerVersion,
            "builtTime" to BuildConfig.BUILD_DATE.format(),
            "runningJdkName" to BuildConfig.BUILD_JDK
        ))
    }

    override fun onEnable(driverContext: AbstractDriver<*>) {
        this.driverContext = driverContext
        driverContext.registerCommand("help", ::onHelp)
        driverContext.registerCommand("?", ::onHelp)
        driverContext.registerCommand("session", ::onSession)
        driverContext.registerCommand("runGC", ::onRunGC)
    }

    private fun onHelp(update: BotUpdate<*>, @Suppress("UNUSED_PARAMETER") commandQueryData: BotCommandQueryData) {
        driverContext.botProvider.sendMessage(update, helpText)
    }

    private fun onSession(update: BotUpdate<*>, @Suppress("UNUSED_PARAMETER") commandQueryData: BotCommandQueryData) {
        val textBuilder = StringBuilder("User ID: ${update.fromUserId}\nChat ID: ${update.chatId}\n===================================\n${update.message.toString()}")
        driverContext.botProvider.sendMessage(update, textBuilder.toString(), update.message?.id)
    }

    private fun onRunGC(update: BotUpdate<*>, @Suppress("UNUSED_PARAMETER") commandQueryData: BotCommandQueryData) {
        if (update.isUserMessage && Defines.systemOptions.auth.admins.contains(update.fromUserId)) {
            System.gc()
            val run = Runtime.getRuntime()
            driverContext.botProvider.sendMessage(update, "GC Finished\n" + "Memory> total:" + run.totalMemory() + " free:" + run.freeMemory() + " used:" + (run.totalMemory()-run.freeMemory()))
        }
    }
}