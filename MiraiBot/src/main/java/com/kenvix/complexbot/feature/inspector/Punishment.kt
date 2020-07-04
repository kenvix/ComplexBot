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

abstract class AbstractPunishment : Named, Logging, Comparable<AbstractPunishment> {
    abstract val description: String
    override fun getLogTag(): String = "Punishment.$name"
    abstract val rank: Int

    /**
     * On message
     * @return boolean Is rule matched and should punish sender
     */
    abstract suspend fun punish(msg: MessageEvent, reason: String)

    protected suspend fun sendPunishmentMessage(msg: MessageEvent, reason: String) {
        val group = msg.subject as Group
        logger.info("${msg.sender.id}(${msg.sender.nameCardOrNick}) in ${group.id}(${group.name}): $reason")

        msg.reply(MessageChainBuilder().apply {
            add(At(msg.sender as Member))
            add(reason)
            add(" <$name>")
        }.build())
    }

    override fun toString(): String {
        return "Punishment($name: $description) Lv.$rank"
    }

    override fun compareTo(other: AbstractPunishment): Int {
        return this.rank.compareTo(other.rank)
    }
}

object Kick : AbstractPunishment() {
    override val name: String = "kick"
    override val description: String = "撤回消息并踢出用户"
    override val rank: Int = 99999999

    override suspend fun punish(msg: MessageEvent, reason: String) {
        msg.source.recall()
        (msg.sender as Member).kick(reason)
        sendPunishmentMessage(msg, reason)
    }
}

object Withdraw : AbstractPunishment() {
    override val name: String = "withdraw"
    override val description: String = "只撤回消息"
    override val rank: Int = 10

    override suspend fun punish(msg: MessageEvent, reason: String) {
        msg.source.recall()
        sendPunishmentMessage(msg, reason)
    }
}

object Noop : AbstractPunishment() {
    override val name: String = "noop"
    override val description: String = "只记录，不执行任何惩罚操作"
    override val rank: Int = 1

    override suspend fun punish(msg: MessageEvent, reason: String) {
        sendPunishmentMessage(msg, reason)
    }
}


class Mute(private val minute: Int) : AbstractPunishment() {
    override val name: String = "mute$minute"
    override val description: String = "禁言 $minute 分钟并撤回消息"
    override val rank: Int = minute + 1000

    override suspend fun punish(msg: MessageEvent, reason: String) {
        msg.source.recall()
        (msg.sender as Member).mute(60 * minute)
        sendPunishmentMessage(msg, reason)
    }
}