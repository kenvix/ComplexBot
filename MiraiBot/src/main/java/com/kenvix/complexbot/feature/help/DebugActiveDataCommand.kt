package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.MessageEvent

object DebugActiveDataCommand : BotCommandFeature {
    override val description: String
        get() = "调试 ActiveData"

    @LowLevelAPI
    override suspend fun onMessage(msg: MessageEvent) {
        val member = msg.sender as Member
        msg.reply(msg.bot._lowLevelGetGroupActiveData(member.group.id).toString())
    }
}