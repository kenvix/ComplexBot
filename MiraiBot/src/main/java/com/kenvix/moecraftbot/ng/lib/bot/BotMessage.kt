//--------------------------------------------------
// Class BotMessage
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.bot

import java.util.*

interface BotMessage {
    val id: Long
    val originalObject: Any?
    val messageFrom: MessageFrom
    val messageType: MessageType
    val messageText: String
    val date: Date
    val sender: BotUser?
    val replyToMessage: BotMessage?
    val extraData: BotExtraData?
    val isEvent: Boolean

    companion object {
        operator fun invoke(
            id: Long,
            messageFrom: MessageFrom = MessageFrom.Unknown,
            messageType: MessageType = MessageType.Other,
            messageText: String = "",
            date: Date = Date(),
            sender: BotUser? = null,
            replyToMessage: BotMessage? = null,
            extraData: BotExtraData? = null
        ): BotMessage {
            return BotMessageImpl(id, messageFrom, messageType, messageText, date, sender, replyToMessage, extraData)
        }
    }
}

data class BotMessageImpl (
    override val id: Long,
    override val messageFrom: MessageFrom = MessageFrom.Unknown,
    override val messageType: MessageType = MessageType.Other,
    override val messageText: String = "",
    override val date: Date = Date(),
    override val sender: BotUser? = null,
    override val replyToMessage: BotMessage? = null,
    override val extraData: BotExtraData? = null,
    override val originalObject: Any? = null
) : BotMessage {
    override val isEvent
        get() = messageType == MessageType.EventGroupMembersLeft || messageType == MessageType.EventGroupMembersJoin
}