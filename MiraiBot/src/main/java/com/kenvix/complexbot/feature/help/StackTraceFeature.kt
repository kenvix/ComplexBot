package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotFeature
import net.mamoe.mirai.Bot

object StackTraceFeature : BotFeature {
    const val MaxStackTraceNumPerGroup = 100
    const val MaxStackTraceNumPerPrivate = 5

    override fun onEnable(bot: Bot) {

    }
}