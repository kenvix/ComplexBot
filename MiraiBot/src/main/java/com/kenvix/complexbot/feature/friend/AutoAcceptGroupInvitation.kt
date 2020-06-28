//--------------------------------------------------
// Class AutoAcceptFriendRequest
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot.feature.friend

import com.kenvix.complexbot.BotFeature
import com.kenvix.complexbot.callBridge
import com.kenvix.complexbot.isBotSystemAdministrator
import com.kenvix.utils.log.Logging
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.FriendAddEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.subscribeAlways

object AutoAcceptGroupInvitation : BotFeature, Logging {
    override fun onEnable(bot: Bot) {
        bot.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            if (callBridge.config.bot.acceptAllGroupInvitation || isBotSystemAdministrator(invitorId)) {
                this.accept()
                logger.info("Accepted to join group $groupId($groupName) by invitor $invitorId($invitorNick)")
            } else {
                this.ignore()
                logger.info("Ignored to join group $groupId($groupName) by invitor $invitorId($invitorNick)")
            }
        }

        bot.subscribeAlways<MemberJoinRequestEvent> {
            logger.trace("Member join: $this")
        }
    }
}