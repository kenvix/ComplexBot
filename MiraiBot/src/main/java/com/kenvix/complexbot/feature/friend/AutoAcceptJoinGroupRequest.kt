package com.kenvix.complexbot.feature.friend

import com.kenvix.complexbot.BotFeature
import com.kenvix.complexbot.callBridge
import com.kenvix.complexbot.command
import com.kenvix.complexbot.feature.middleware.AdminPermissionRequired
import com.kenvix.complexbot.feature.middleware.GroupMessageOnly
import com.kenvix.complexbot.feature.middleware.SwitchableCommand
import com.kenvix.utils.log.Logging
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeMessages
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

object AutoAcceptJoinGroupRequest : BotFeature, Logging {
    const val EnabledKeyName = "AutoAcceptJoinGroupEnabled"
    const val MatchRuleName = "AutoAcceptJoinGroupMatchRule"
    internal val patternCache: MutableMap<Long, Pattern> = ConcurrentHashMap()

    override fun onEnable(bot: Bot) {
        bot.subscribeMessages {
            command("autoaccept", AutoAcceptJoinGroupOptionCommand, AdminPermissionRequired, GroupMessageOnly, SwitchableCommand)
        }
        bot.subscribeAlways<MemberJoinRequestEvent> {
            logger.trace("Member join requested: ${this.fromId}($fromNick)[$message] in group $groupId($groupName)")
            if (isMatched(groupId, message)) {
                logger.info("Matched user ${this.fromId}($fromNick)[$message] in group $groupId($groupName)")

                if (group.botPermission.isOperator())
                    this.accept()
            }
        }
    }


    fun isMatched(groupId: Long, messageRaw: String): Boolean {
        val op = callBridge.getGroupOptions(groupId).options

        if (op[EnabledKeyName] == "true" && !op[MatchRuleName].isNullOrBlank()) {
            val message = messageRaw.replace("\n", "").replace("\r", "").trim()
            val pattern = patternCache[groupId].run {
                if (this == null) {
                    patternCache[groupId] =
                        Pattern.compile(op[MatchRuleName]!!)
                    patternCache[groupId]!!
                } else {
                    this
                }
            }

            return pattern.matcher(message).matches()
        }

        return false
    }
}