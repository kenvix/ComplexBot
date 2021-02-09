package com.kenvix.complexbot.feature.switchcommand

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.callBridge
import com.kenvix.complexbot.commands
import com.kenvix.complexbot.feature.middleware.SwitchableCommand
import com.kenvix.complexbot.reply
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content

object SwitchCommand : BotCommandFeature {
    override val description: String
        get() = "禁用或启用命令"

    override suspend fun onMessage(msg: MessageEvent) {
        val command = parseCommandFromMessage(msg.message.content, false)

        if (command.arguments.isNotEmpty() && (command.command == "disable" || command.command == "enable")) {
            if (commands[command.firstArgument]?.middlewares?.contains(SwitchableCommand) == true) {
                callBridge.getGroupOptions((msg.sender as Member).group.id).run {
                    if (command.command == "disable") {
                        disabledCommands.add(command.firstArgument)
                        msg.reply("已在本群禁用命令：${command.firstArgument}")
                    } else {
                        disabledCommands.remove(command.firstArgument)
                        msg.reply("已在本群启用命令：${command.firstArgument}")
                    }
                    callBridge.saveGroupOptions(this)
                }
            } else {
                msg.reply("不存在此命令或该命令不支持开关：${command.firstArgument}")
            }
        } else {
            msg.reply("使用方法：\n启用命令：.enable 命令\n" +
                    "禁用命令：.disable 命令\n" +
                    "列出命令：.listcommands")
        }
    }
}