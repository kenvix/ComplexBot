package com.kenvix.complexbot.feature

import com.kenvix.complexbot.addFeature
import com.kenvix.complexbot.command
import com.kenvix.complexbot.feature.friend.AutoAcceptFriendRequest
import com.kenvix.complexbot.feature.friend.AutoAcceptGroupInvitation
import com.kenvix.complexbot.feature.friend.WelcomeNewFeature
import com.kenvix.complexbot.feature.help.*
import com.kenvix.complexbot.feature.inspector.InspectorFeature
import com.kenvix.complexbot.feature.inspector.GroupRankingCommand
import com.kenvix.complexbot.feature.inspector.PunishCommand
import com.kenvix.complexbot.feature.inspector.WhoCommand
import com.kenvix.complexbot.feature.middleware.AdminPermissionRequired
import com.kenvix.complexbot.feature.middleware.CorePermissionRequired
import com.kenvix.complexbot.feature.middleware.GroupMessageOnly
import com.kenvix.complexbot.feature.middleware.SwitchableCommand
import com.kenvix.complexbot.feature.sex.LifePredictorCommand
import com.kenvix.complexbot.feature.sex.RepeatCommand
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
        command("relogin", ReLoginCommand, CorePermissionRequired)

//        command("sex", SexCommand, SwitchableCommand)
//        command("文爱", SexCommand, SwitchableCommand)

        command("算卦", LifePredictorCommand, SwitchableCommand)
        command("占卜", LifePredictorCommand, SwitchableCommand)
        command("predict", LifePredictorCommand, SwitchableCommand)

        command("enable", SwitchCommand, GroupMessageOnly, AdminPermissionRequired)
        command("disable", SwitchCommand, GroupMessageOnly, AdminPermissionRequired)
        command("p", PunishCommand, GroupMessageOnly, AdminPermissionRequired)
        command("listcommands", ListCommand, SwitchableCommand)
        command("uptime", UpTimeCommand, SwitchableCommand)

        command("rank", GroupRankingCommand, GroupMessageOnly, SwitchableCommand)
        command("排行", GroupRankingCommand, GroupMessageOnly, SwitchableCommand)
        command("who", WhoCommand, GroupMessageOnly, SwitchableCommand)

        command("repeat", RepeatCommand, GroupMessageOnly, SwitchableCommand, AdminPermissionRequired)
    }

    addFeature(InspectorFeature)
    addFeature(WelcomeNewFeature)

    addFeature(AutoAcceptFriendRequest)
    addFeature(AutoAcceptGroupInvitation)
}