package com.kenvix.complexbot.feature.inspector

import com.kenvix.moecraftbot.ng.lib.Named
import com.kenvix.moecraftbot.ng.lib.createNamedElementsMap
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageEvent

val punishments = createNamedElementsMap(Kick, Withdraw, Noop)

interface Punishment : Named {
    val description: String

    /**
     * On message
     * @return boolean Is rule matched and should punish sender
     */
    suspend fun punish(msg: MessageEvent, user: User)
}

object Kick : Punishment {
    override val name: String
        get() = "kick"
    override val description: String
        get() = "撤回消息并踢出用户"

    override suspend fun punish(msg: MessageEvent, user: User) {

    }
}

object Withdraw : Punishment {
    override val name: String
        get() = "withdraw"
    override val description: String
        get() = "只撤回消息"

    override suspend fun punish(msg: MessageEvent, user: User) {

    }
}

object Noop : Punishment {
    override val name: String
        get() = "noop"
    override val description: String
        get() = "只记录，不执行任何惩罚操作"

    override suspend fun punish(msg: MessageEvent, user: User) {

    }
}


abstract class Mute : Punishment {
    override val name: String
        get() = "mute"
    override val description: String
        get() = "禁言"

    override suspend fun punish(msg: MessageEvent, user: User) {

    }
}