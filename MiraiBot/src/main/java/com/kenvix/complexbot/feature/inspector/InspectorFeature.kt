package com.kenvix.complexbot.feature.inspector

import com.kenvix.android.utils.Coroutines
import com.kenvix.complexbot.*
import com.kenvix.complexbot.feature.middleware.AdminPermissionRequired
import com.kenvix.complexbot.feature.middleware.GroupMessageOnly
import com.kenvix.moecraftbot.ng.Defines
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.MemberLeaveEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeMessages
import org.litote.kmongo.MongoOperator
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object InspectorFeature : BotFeature {
    // Note: 犹豫要不要 @Synchronized，用了性能降低，不用可能发生设置不一致
    private val inspectorOptions: MutableMap<Long, InspectorInternalOptions> = ConcurrentHashMap()
    private val coroutines = Coroutines()

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
                        this.sender.permission == MemberPermission.MEMBER &&
                        this.sender.id !in callBridge.config.bot.administratorIds
                    ) {
                        inspectorOptions.rules.map { (rule, punishment) ->
                            RuleResult(coroutines.ioScope.async {
                                rule.onMessage(this@always)
                            }, rule, punishment)
                        }.filter {
                            kotlin.runCatching { it.result.await() }.getOrNull() == true
                        }.maxByOrNull {
                            it.punishment
                        }.also {
                            if (it != null) {
                                this@always.executeCatchingBusinessException {
                                    it.punishment.punish(this@always, it.rule.punishReason)
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
            val result: Deferred<Boolean>,
            val rule: InspectorRule,
            val punishment: AbstractPunishment
    )

    private data class InspectorInternalOptions(
            val options: InspectorOptions,
            val rules: Map<InspectorRule, AbstractPunishment>
    ) {
        val white: Set<Long>
            get() = options.white

        constructor(options: InspectorOptions): this(options, options.rules.filter { entry ->
            entry.key in inspectorRules && entry.value in punishments
        }.map { entry ->
            (inspectorRules[entry.key] ?: error("")) to (punishments[entry.value] ?: error(""))
        }.toMap())
    }
}