package com.kenvix.complexbot.feature.inspector

import com.kenvix.android.utils.Coroutines
import com.kenvix.complexbot.*
import com.kenvix.complexbot.feature.middleware.AdminPermissionRequired
import com.kenvix.complexbot.feature.middleware.GroupMessageOnly
import com.kenvix.moecraftbot.ng.Defines
import com.kenvix.moecraftbot.ng.lib.asFlow
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.MemberLeaveEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeMessages
import org.litote.kmongo.MongoOperator
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object InspectorFeature : BotFeature {
    // Note: 犹豫要不要 @Synchronized，用了性能降低，不用可能发生设置不一致
    private val inspectorOptions: MutableMap<Long, InspectorInternalOptions> = ConcurrentHashMap()
    private val coroutines = Coroutines()
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun onEnable(bot: Bot) {
        runBlocking {
            callBridge.getAllGroupOptions()
                .filter { it.inspector.enabled }
                .forEach { applyInspectorOptions(it.groupId, it.inspector) }
        }

        bot.subscribeMessages {
            command("inspector", InspectorCommand, GroupMessageOnly, AdminPermissionRequired)
        }

        bot.subscribeAlways<MemberJoinRequestEvent> {
            inspectorOptions[this.group.id]?.also {
                logger.debug("Inspected member join request: ${group.id}(${group.name}) / $fromId($fromNick): $message")
                InspectorStatisticUtils.putMemberJoinStat(this)
            }
        }

        bot.subscribeAlways<MemberJoinEvent> event@ {
            inspectorOptions[this.group.id]?.also {
                logger.debug("Inspected member join accepted:${group.id}(${group.name}) / ${member.id}(${member.nameCardOrNick})")
                InspectorStatisticUtils.getStat(group.id).joins[member.id]?.run stat@ {
                    //TODO: Implement MIRAI Inviter info getter
                    status = JoinStatus.Accepted.statusId
                }
            }
        }

        bot.subscribeAlways<MemberLeaveEvent> {
            inspectorOptions[this.group.id]?.also {
                logger.debug("Inspected member join left:${group.id}(${group.name}) / ${member.id}(${member.nameCardOrNick})")
                InspectorStatisticUtils.updateMemberJoinStatus(member.id, group.id, JoinStatus.Left.statusId)
            }
        }

        bot.subscribeGroupMessages {
            always {
                inspectorOptions[this.group.id]?.also { inspectorOptions ->
                    var isPunished = false
                    if (this.sender.id !in inspectorOptions.white &&
                        !this.sender.permission.isOperator() &&
                        this.sender.id !in callBridge.config.bot.administratorIds
                    ) {
                        inspectorOptions.rules.asFlow().map { (rule, punishment) ->
                            kotlin.runCatching {
                                RuleResult(rule.onMessage(this@always), rule, punishment)
                            }.onFailure { exception ->
                                logger.warn("Inspector rule failed: ${rule.name}:${punishment.name} [Group ${this.group.id}]", exception)
                            }.getOrNull()
                        }.filterNotNull().filter {
                            it.result
                        }.run {
                            if (count() > 1)
                                toList().maxByOrNull { it.punishment.rank }
                            else
                                first()
                        }.also { //TODO
                            if (it != null) {
                                this@always.executeCatchingBusinessException {
                                    it.punishment.punish(this@always, it.rule.punishReason, it.rule)
                                }

                                isPunished = true
                            }
                        }
                    }

                    InspectorStatisticUtils.addMemberCountStat(sender, isPunished)
                }
            }
        }
    }

    fun applyInspectorOptions(group: Long, config: InspectorOptions) {
        if (config.enabled) {
            inspectorOptions[group] = InspectorInternalOptions(config)
        } else {
            inspectorOptions.remove(group)
        }
    }

    private data class RuleResult(
            val result: Boolean,
            val rule: InspectorRule,
            val punishment: Punishment
    )

    private data class InspectorInternalOptions(
            val options: InspectorOptions,
            val rules: Map<InspectorRule, Punishment>
    ) {
        val white: Set<Long>
            get() = options.white

        constructor(options: InspectorOptions): this(options, options.rules.asSequence().filter { entry ->
            entry.key in inspectorRules && entry.value in punishments
        }.map { entry ->
            (inspectorRules[entry.key] ?: error("")) to (punishments[entry.value] ?: error(""))
        }.sortedBy {
            it.second.rank
        }.toMap())
    }
}