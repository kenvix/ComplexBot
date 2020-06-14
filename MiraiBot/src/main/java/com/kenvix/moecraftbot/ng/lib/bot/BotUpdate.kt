//--------------------------------------------------
// Class BotUpdate
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.bot

interface BotUpdate<T> {
    val updateObject: T
    val message: BotMessage?
    val chatId: Long
    val updateId: Long
    val hasMessage: Boolean
    val fromUserId: Long
    val isUserMessage: Boolean

    companion object {
        operator fun <T> invoke(
            updateObject: T,
            message: BotMessage? = null,
            chatId: Long = 0,
            updateId: Long = 0
        ): BotUpdate<T> {
            return BotUpdateImpl<T>(updateObject, message, chatId, updateId)
        }
    }
}

//TODO wrap total tg message system
class BotUpdateImpl<T> (
    override val updateObject: T,
    override val message: BotMessage? = null,
    override val chatId: Long = 0,
    override val updateId: Long = 0
) : BotUpdate<T>
{
    override fun toString(): String {
        return updateObject.toString()
    }

    override val hasMessage: Boolean
        get() = message != null

    override val fromUserId: Long
        get() = message?.sender?.id ?: 0L

    override val isUserMessage
        get() = fromUserId != 0L
}