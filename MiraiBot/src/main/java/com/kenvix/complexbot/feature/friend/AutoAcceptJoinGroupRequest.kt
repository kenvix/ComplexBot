package com.kenvix.complexbot.feature.friend

import com.kenvix.complexbot.BotFeature
import com.kenvix.complexbot.callBridge
import com.kenvix.utils.log.Logging
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.subscribeAlways
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

object AutoAcceptJoinGroupRequest : BotFeature, Logging {
    const val EnabledKeyName = "AutoAcceptJoinGroupEnabled"
    const val MatchRuleName = "AutoAcceptJoinGroupMatchRule"
    internal val patternCache: MutableMap<Long, Pattern> = ConcurrentHashMap()

    override fun onEnable(bot: Bot) {
        bot.subscribeAlways<MemberJoinRequestEvent> {
            logger.trace("Member join requested: $this")
            val op = callBridge.getGroupOptions(groupId).options

            if (op[EnabledKeyName] == "true" && !op[MatchRuleName].isNullOrBlank()) {
                val pattern = patternCache[groupId].run {
                    if (this == null) {
                        patternCache[groupId] = Pattern.compile(op[MatchRuleName]!!)
                        patternCache[groupId]!!
                    } else {
                        this
                    }
                }

                if (pattern.matcher(message).matches()) {
                    logger.info("Matched user ${this.fromId}($fromNick)[$message] in group $groupId($groupName)")

                    if (group.botPermission.isOperator())
                        this.accept()
                }
            }
        }
    }
}