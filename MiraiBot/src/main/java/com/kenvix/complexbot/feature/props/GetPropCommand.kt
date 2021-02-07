package com.kenvix.complexbot.feature.props

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.callBridge
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.MessageEvent

object GetPropCommand : BotCommandFeature {
    override val description: String = "获取属性"

    override suspend fun onMessage(msg: MessageEvent) {
        val group = (msg.sender as Member).group
        val op = callBridge.getGroupOptions(group.id)

    }
}