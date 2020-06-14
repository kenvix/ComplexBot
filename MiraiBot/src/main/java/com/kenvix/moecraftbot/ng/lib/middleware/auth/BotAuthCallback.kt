//--------------------------------------------------
// Interface BotAuthCallback
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.middleware.auth

import com.kenvix.moecraftbot.ng.lib.bot.AbstractDriver
import com.kenvix.moecraftbot.ng.lib.bot.BotCommandQueryData
import com.kenvix.moecraftbot.ng.lib.bot.BotUpdate
import com.kenvix.moecraftbot.ng.lib.bot.BotUser
import com.kenvix.moecraftbot.ng.lib.exception.BusinessLogicException
import com.kenvix.moecraftbot.ng.orm.tables.pojos.Auths

abstract class BotAuthCallback {
    lateinit var driverContext: AbstractDriver<*>
        private set

    open fun onEnable(driverContext: AbstractDriver<*>) {
        this.driverContext = driverContext
    }

    @Throws(BusinessLogicException::class)
    open fun onAuthSessionBegin(update: BotUpdate<*>, command: BotCommandQueryData, sessionBot: BotAuthSession) {

    }

    @Throws(BusinessLogicException::class)
    open fun onAuthInfoQuery(update: BotUpdate<*>, commandQueryData: BotCommandQueryData, authInfo: Auths, resultBuilder: StringBuilder) {

    }

    @Throws(BusinessLogicException::class)
    open fun onNewMemberJoin(update: BotUpdate<*>, user: BotUser, authInfo: Auths) {

    }

    @Throws(BusinessLogicException::class)
    abstract fun onAuthSiteRequest(update: BotUpdate<*>, sessionBot: BotAuthSession)

    @Throws(BusinessLogicException::class)
    open fun onAuthSessionSuccessfullyEnd(update: BotUpdate<*>, sessionBot: BotAuthSession) {

    }
}