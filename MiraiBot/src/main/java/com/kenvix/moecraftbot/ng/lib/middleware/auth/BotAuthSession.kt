//--------------------------------------------------
// Class AuthSession
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.middleware.auth

data class BotAuthSession(val userId: Long) {
    var userName: String = "" // Telegram Name
    var status: BotAuthStatus = BotAuthStatus.Idle
    var data: BotAuthData = BotAuthData("")
}