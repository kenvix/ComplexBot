//--------------------------------------------------
// Interface BotMiddleware
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.middleware

import com.kenvix.moecraftbot.ng.lib.bot.AbstractDriver

interface BotMiddleware {
    fun onEnable(driverContext: AbstractDriver<*>)
}