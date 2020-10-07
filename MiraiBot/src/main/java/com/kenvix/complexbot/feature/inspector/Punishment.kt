package com.kenvix.complexbot.feature.inspector

import com.kenvix.moecraftbot.ng.lib.Named
import com.kenvix.moecraftbot.ng.lib.createNamedElementsMap
import com.kenvix.utils.log.Logging
import kotlinx.coroutines.delay
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.MessageSource

val punishments = createNamedElementsMap(
    Kick,
    Withdraw,
    AtAdministrators,
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

interface Punishment : Named, Comparable<Punishment>, Logging {
    val description: String
    val rank: Int
    /**
     * On message
     * @return boolean Is rule matched and should punish sender
     */
    suspend fun punish(group: Group, target: Member, reason: String, msgSrc: MessageSource? = null, rule: InspectorRule? = null)

    suspend fun punish(msg: MessageEvent, reason: String, rule: InspectorRule? = null) {
        punish(msg.subject as Group, msg.sender as Member, reason, msg.source, rule)
    }
}

abstract class AbstractPunishment : Punishment {

    override fun getLogTag(): String = "Punishment.$name"

    protected suspend fun sendPunishmentMessage(sender: Member, group: Group, reason: String, rule: InspectorRule? = null,
                                                extra: ((MessageChainBuilder) -> Unit)? = null) {

        logger.info("${sender.id}(${sender.nameCardOrNick}) in ${group.id}(${group.name}): $reason")

        group.sendMessage(MessageChainBuilder().apply {
            add("检测到违法用户：")
            add(At(sender))
            add(reason)
            add(" <规则名: ${rule?.name} | 惩罚: $name>")
            extra?.invoke(this)
        }.build())
    }

    override fun toString(): String {
        return "Punishment($name: $description) Lv.$rank"
    }

    override fun compareTo(other: Punishment): Int {
        return this.rank.compareTo(other.rank)
    }
}

object Kick : AbstractPunishment() {
    override val name: String = "kick"
    override val description: String = "撤回消息并踢出用户"
    override val rank: Int = 99999999

    override suspend fun punish(group: Group, target: Member, reason: String, msgSrc: MessageSource?, rule: InspectorRule?) {
        msgSrc?.bot?.recall(msgSrc)
        target.kick(reason)

        sendPunishmentMessage(target, group, reason, rule)
    }
}

object Withdraw : AbstractPunishment() {
    override val name: String = "withdraw"
    override val description: String = "只撤回消息"
    override val rank: Int = 10

    override suspend fun punish(group: Group, target: Member, reason: String, msgSrc: MessageSource?, rule: InspectorRule?) {
        msgSrc.also {
            if (it == null)  {
                sendPunishmentMessage(target, group, "无法进行操作：MessageSource cannot be null", rule)
            } else {
                it.bot.recall(it)
                sendPunishmentMessage(target, group, reason, rule)
            }
        }
    }
}

object Noop : AbstractPunishment() {
    override val name: String = "noop"
    override val description: String = "只记录，不执行任何惩罚操作"
    override val rank: Int = 1

    override suspend fun punish(group: Group, target: Member, reason: String, msgSrc: MessageSource?, rule: InspectorRule?) {
        sendPunishmentMessage(target, group, reason, rule)
    }
}

object AtAdministrators : AbstractPunishment() {
    override val name: String = "at"
    override val description: String = "只呼叫全体管理员进行处理"
    override val rank: Int = 5

    override suspend fun punish(group: Group, target: Member, reason: String, msgSrc: MessageSource?, rule: InspectorRule?) {
        sendPunishmentMessage(target, group, reason, rule) { builder ->
            builder.add("\n请管理员处理。（以下为按照本群规则设置的自动呼叫）")
        }

        target.group.members.asSequence().filter {
            it.permission.level in 1..2
        }.map {
            At(it)
        }.chunked(5).forEach {  chunk ->
            delay(350)
            group.sendMessage(MessageChainBuilder().apply {
                chunk.forEach { add(it) }
            }.build())
        }
    }
}

class Mute(private val minute: Int) : AbstractPunishment() {
    override val name: String = "mute$minute"
    override val description: String = "禁言 $minute 分钟并撤回消息"
    override val rank: Int = minute + 1000

    override suspend fun punish(group: Group, target: Member, reason: String, msgSrc: MessageSource?, rule: InspectorRule?) {
        msgSrc?.bot?.recall(msgSrc)
        target.mute(60 * minute)

        sendPunishmentMessage(target, group, reason, rule)
    }
}