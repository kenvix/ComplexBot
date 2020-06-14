@file:JvmName("BotUtils")
package com.kenvix.moecraftbot.ng.lib.bot

val MessageType.isEvent
        get() = this == MessageType.EventGroupMembersLeft || this == MessageType.EventGroupMembersJoin