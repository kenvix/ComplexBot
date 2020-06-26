//--------------------------------------------------
// Interface BotMiddleware
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.mirai.middleware

import com.kenvix.moecraftbot.mirai.lib.bot.AbstractDriver

interface BotMiddleware {
    fun onEnable(driverContext: AbstractDriver<*>)
}