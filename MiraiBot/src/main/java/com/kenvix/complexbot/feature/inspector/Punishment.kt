package com.kenvix.complexbot.feature.inspector

import com.kenvix.moecraftbot.ng.lib.Named
import com.kenvix.moecraftbot.ng.lib.createNamedElementsMap
import com.kenvix.utils.log.Logging
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.recall

val punishments = createNamedElementsMap(
    Kick,
    Withdraw,
    Mute(1),
    Mute(5),
    Mute(20),
    Mute(60),
    Mute(300),
    Mute(1440),
    Mute(7200),
    Mute(14400),
    Mute(43199),
    Noop)

interface Punishment : Named, Logging {
    val description: String
    override fun getLogTag(): String = "Punishment.$name"

    /**
     * On message
     * @return boolean Is rule matched and should punish sender
     */
    suspend fun punish(msg: MessageEvent, reason: String)

    suspend fun sendPunishmentMessage(msg: MessageEvent, reason: String) {
        val group = msg.subject as Group
        logger.info("${msg.sender.id}(${msg.sender.nameCardOrNick}) in ${group.id}(${group.name}): $reason")

        msg.reply(MessageChainBuilder().apply {
            add(At(msg.sender as Member))
            add(reason)
            add(" <$name>")
        }.build())
    }
}

object Kick : Punishment {
    override val name: String
        get() = "kick"
    override val description: String
        get() = "撤回消息并踢出用户"

    override suspend fun punish(msg: MessageEvent, reason: String) {
        msg.source.recall()
        (msg.sender as Member).kick(reason)
        sendPunishmentMessage(msg, reason)
    }
}

object Withdraw : Punishment {
    override val name: String
        get() = "withdraw"
    override val description: String
        get() = "只撤回消息"

    override suspend fun punish(msg: MessageEvent, reason: String) {
        msg.source.recall()
        sendPunishmentMessage(msg, reason)
    }
}

object Noop : Punishment {
    override val name: String
        get() = "noop"
    override val description: String
        get() = "只记录，不执行任何惩罚操作"

    override suspend fun punish(msg: MessageEvent, reason: String) {
        sendPunishmentMessage(msg, reason)
    }
}


class Mute(private val minute: Int) : Punishment {
    override val name: String
        get() = "mute$minute"
    override val description: String
        get() = "禁言 $minute 分钟并撤回消息"

    override suspend fun punish(msg: MessageEvent, reason: String) {
        msg.source.recall()
        (msg.sender as Member).mute(60 * minute)
        sendPunishmentMessage(msg, reason)
    }
}