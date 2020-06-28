package com.kenvix.complexbot.feature

import com.kenvix.complexbot.addFeature
import com.kenvix.complexbot.command
import com.kenvix.complexbot.feature.friend.AutoAcceptFriendRequest
import com.kenvix.complexbot.feature.friend.AutoAcceptGroupInvitation
import com.kenvix.complexbot.feature.help.DebugCommand
import com.kenvix.complexbot.feature.help.HelpCommand
import com.kenvix.complexbot.feature.inspector.InspectorFeature
import com.kenvix.complexbot.feature.middleware.AdminPermissionRequired
import com.kenvix.complexbot.feature.middleware.GroupMessageOnly
import com.kenvix.complexbot.feature.sex.SexCommand
import com.kenvix.complexbot.feature.sex.SexSwitchCommand
import com.kenvix.complexbot.feature.switchall.SwitchAllCommand
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeMessages

fun Bot.featureRoutes() {
    subscribeMessages {
        command("help", HelpCommand)
        command("debug", DebugCommand)

        command("sex", SexCommand)

        command("switch sex", SexSwitchCommand, GroupMessageOnly, AdminPermissionRequired)
        command("switch all", SwitchAllCommand, GroupMessageOnly, AdminPermissionRequired)
    }

    addFeature(InspectorFeature)
    addFeature(AutoAcceptFriendRequest)
    addFeature(AutoAcceptGroupInvitation)
}