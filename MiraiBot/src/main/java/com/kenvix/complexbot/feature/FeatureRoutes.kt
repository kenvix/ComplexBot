package com.kenvix.complexbot.feature

import com.kenvix.complexbot.CallBridge
import com.kenvix.complexbot.addFeature
import com.kenvix.complexbot.command
import com.kenvix.complexbot.feature.help.DebugCommand
import com.kenvix.complexbot.feature.inspector.InspectorCommand
import com.kenvix.complexbot.feature.help.HelpCommand
import com.kenvix.complexbot.feature.inspector.InspectorFeature
import com.kenvix.complexbot.feature.middleware.AdminPermissionRequiredIfInGroup
import com.kenvix.complexbot.feature.middleware.GroupMessageOnly
import com.kenvix.complexbot.feature.sex.SexCommand
import com.kenvix.complexbot.feature.sex.SexSwitchCommand
import com.kenvix.complexbot.feature.switchall.SwitchAllCommand
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeMessages

fun Bot.featureRoutes(b: CallBridge) {
    subscribeMessages {
        command(b, "help", HelpCommand)
        command(b,"debug", DebugCommand)

        command(b,"sex", SexCommand)

        command(b,"switch sex", SexSwitchCommand, GroupMessageOnly, AdminPermissionRequiredIfInGroup)
        command(b,"switch all", SwitchAllCommand, GroupMessageOnly, AdminPermissionRequiredIfInGroup)
    }

    addFeature(b, InspectorFeature)
}