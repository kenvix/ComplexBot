package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.CallBridge
import com.kenvix.complexbot.callBridge
import com.kenvix.moecraftbot.ng.Defines
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isFriend
import net.mamoe.mirai.contact.isMuted
import net.mamoe.mirai.message.MessageEvent
import java.text.SimpleDateFormat
import java.util.*

object DebugActiveDataCommand : BotCommandFeature {
    override val description: String
        get() = "调试 ActiveData"

    @LowLevelAPI
    override suspend fun onMessage(msg: MessageEvent) {
        val member = msg.sender as Member
        msg.reply(msg.bot._lowLevelGetGroupActiveData(member.group.id).toString())
    }
}