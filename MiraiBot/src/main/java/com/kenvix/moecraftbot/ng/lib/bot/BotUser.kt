//--------------------------------------------------
// Class BotUser
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.bot

interface BotUser {
    val id: Long
    val name: String
    val description: String

    companion object {
        operator fun invoke(
            id: Long,
            name: String = "",
            description: String = ""
        ): BotUser {
            return BotUserImpl(id, name, description)
        }
    }
}

data class BotUserImpl (
    override val id: Long,
    override val name: String = "",
    override val description: String = ""
) : BotUser {
    override fun toString(): String {
        return "@$name [$description]:$id"
    }
}