//--------------------------------------------------
// Enum MessageFrom
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.bot

enum class MessageFrom {
    Private, Group, Channel, Unknown, Discussion;

    companion object {
        @JvmStatic
        fun getMessageFromFromString(string: String) = when(string.toLowerCase()) {
            "private" -> Private
            "group" -> Group
            "channel" -> Channel
            "discussion" -> Discussion
            else -> Unknown
        }
    }
}