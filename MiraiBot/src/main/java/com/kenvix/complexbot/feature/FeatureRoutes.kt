package com.kenvix.complexbot.feature

import com.kenvix.complexbot.command
import com.kenvix.complexbot.feature.adfilter.AdFilterCommand
import com.kenvix.complexbot.feature.help.HelpCommand
import com.kenvix.complexbot.feature.middleware.AdminPermissionRequiredIfInGroup
import com.kenvix.complexbot.feature.middleware.GroupMessageOnly
import com.kenvix.complexbot.feature.sex.SexCommand
import com.kenvix.complexbot.feature.sex.SexSwitchCommand
import com.kenvix.complexbot.feature.switch_all.SwitchAllCommand
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeMessages

fun Bot.featureRoutes() {
    subscribeMessages {
        command("help", HelpCommand)
        command("sex", SexCommand)

        command("switch adfilter", AdFilterCommand, GroupMessageOnly, AdminPermissionRequiredIfInGroup)
        command("switch sex", SexSwitchCommand, GroupMessageOnly, AdminPermissionRequiredIfInGroup)
        command("switch all", SwitchAllCommand, GroupMessageOnly, AdminPermissionRequiredIfInGroup)
    }
}