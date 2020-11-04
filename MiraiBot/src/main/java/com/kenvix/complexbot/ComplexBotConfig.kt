//--------------------------------------------------
// Class ComplexBotConfig
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------
package com.kenvix.complexbot

class ComplexBotConfig {
    @JvmField var bot: Bot = Bot()
    @JvmField var mirai: Mirai = Mirai()

    class Bot {
        @JvmField var name: String = "ComplexBot"
        @JvmField var password: String = "123456"
        @JvmField var qq: Long = 0
        @JvmField var administratorIds: List<Long> = emptyList()
        @JvmField var acceptAllFriendRequest = false
        @JvmField var acceptAllGroupInvitation = false
    }

    class Mirai {
        @JvmField var authKey: String = ""
        @JvmField var port = 0
        @JvmField var protocol: String = "phone"
    }
}