//--------------------------------------------------
// Class AuthData
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.middleware.auth

data class BotAuthData(var email: String) {
    var password: String = ""
    var uid: Int? = null
    var name: String? = null
    var token: String? = null
    var userData: Any? = null

    override fun toString(): String {
        return "#$uid: $name"
    }
}