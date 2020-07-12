package com.kenvix.complexbot.feature

import com.kenvix.complexbot.addFeature
import com.kenvix.complexbot.command
import com.kenvix.complexbot.feature.friend.AutoAcceptFriendRequest
import com.kenvix.complexbot.feature.friend.AutoAcceptGroupInvitation
import com.kenvix.complexbot.feature.friend.WelcomeNewFeature
import com.kenvix.complexbot.feature.help.DebugActiveDataCommand
import com.kenvix.complexbot.feature.help.DebugCommand
import com.kenvix.complexbot.feature.help.HelpCommand
import com.kenvix.complexbot.feature.help.UpTimeCommand
import com.kenvix.complexbot.feature.inspector.InspectorFeature
import com.kenvix.complexbot.feature.middleware.AdminPermissionRequired
import com.kenvix.complexbot.feature.middleware.GroupMessageOnly
import com.kenvix.complexbot.feature.middleware.SwitchableCommand
import com.kenvix.complexbot.feature.sex.LifePredictorCommand
import com.kenvix.complexbot.feature.sex.SexCommand
import com.kenvix.complexbot.feature.switchcommand.ListCommand
import com.kenvix.complexbot.feature.switchcommand.SwitchCommand
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeMessages

fun Bot.featureRoutes() {
    subscribeMessages {
        command("help", HelpCommand, SwitchableCommand)
        command("帮助", HelpCommand, SwitchableCommand)
        command("debug", DebugCommand, SwitchableCommand)
        command("debugactivedata", DebugActiveDataCommand, GroupMessageOnly, SwitchableCommand)

        command("sex", SexCommand, SwitchableCommand)
        command("文爱", SexCommand, SwitchableCommand)

        command("算卦", LifePredictorCommand, SwitchableCommand)
        command("占卜", LifePredictorCommand, SwitchableCommand)
        command("predict", LifePredictorCommand, SwitchableCommand)

        command("enable", SwitchCommand, GroupMessageOnly, AdminPermissionRequired)
        command("disable", SwitchCommand, GroupMessageOnly, AdminPermissionRequired)
        command("listcommands", ListCommand)
        command("uptime", UpTimeCommand)
    }

    addFeature(InspectorFeature)
    addFeature(WelcomeNewFeature)

    addFeature(AutoAcceptFriendRequest)
    addFeature(AutoAcceptGroupInvitation)
}