package com.kenvix.complexbot.feature.inspector

import com.kenvix.android.utils.Coroutines
import com.kenvix.complexbot.*
import com.kenvix.complexbot.feature.middleware.AdminPermissionRequired
import com.kenvix.complexbot.feature.middleware.GroupMessageOnly
import com.kenvix.moecraftbot.ng.lib.asFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.MemberLeaveEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeMessages
import org.slf4j.LoggerFactory
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
                        inspectorOptions.actualRules.asFlow().map { (rule, placeHolders) ->
                            kotlin.runCatching {
                                val ruleResult = rule.onMessage(this@always, placeHolders)
                                RuleResult(ruleResult, ruleResult?.let {
                                    if (inspectorOptions.rules[it] != null)
                                        inspectorOptions.rules[it]
                                    else
                                        null
                                })
                            }.onFailure { exception ->
                                if (exception !is CancellationException && exception.cause == null)
                                    logger.warn("Inspector rule failed: ${rule.name} [Group ${this.group.id}(${group.name})]", exception)
                            }.getOrNull()
                        }.filter {
                            it?.rule != null
                        }.take(1).collect {
                            if (it != null) {
                                this@always.executeCatchingBusinessException {
                                    it.punishment?.punish(this@always, it.rule!!.punishReason, it.rule)
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
            val rule: InspectorRule?,
            val punishment: Punishment?
    )

    private data class InspectorInternalOptions(
            val options: InspectorOptions,
            val rules: Map<InspectorRule, Punishment>
    ) {
        val white: Set<Long>
            get() = options.white

        val actualRules by lazy {
            rules.asSequence().filter {
                it.key is InspectorRule.Placeholder
            }.groupBy(keySelector = {
                (it.key as InspectorRule.Placeholder).actualRule
            }, valueTransform = {
                it.key as InspectorRule.Placeholder
            }) + rules.asSequence().filter {
                it.key is InspectorRule.Actual
            }.map {
                it.key as InspectorRule.Actual to emptyList<InspectorRule.Placeholder>()
            }.toMap()
        }

        constructor(options: InspectorOptions) : this(options, options.rules.asSequence().filter { entry ->
            entry.key in inspectorRules && entry.value in punishments
        }.map { entry ->
            (inspectorRules[entry.key] ?: error("Rule not found ${entry.key}")) to
                    ((punishments[entry.value] ?: error("Punishment not found: ${entry.value}")))
        }.sortedBy {
            it.second.rank
        }.toMap())
    }
}