//--------------------------------------------------
// Class ChatSession
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.bot

import com.kenvix.moecraftbot.ng.lib.CHAT_TYPE_IDLE

data class ChatSession(var chatTypeCode: Int = CHAT_TYPE_IDLE, var extraData: Any? = null) {

    @Suppress("UNCHECKED_CAST")
    fun <R> getExtraData(@Suppress("UNUSED_PARAMETER") typeClass: Class<R>): R {
        return extraData as R
    }
}