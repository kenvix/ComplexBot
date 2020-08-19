package com.kenvix.complexbot.feature.switchcommand

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.commands
import net.mamoe.mirai.message.MessageEvent

object ListCommand : BotCommandFeature {
    override val description: String = "列出命令列表"

    override suspend fun onMessage(msg: MessageEvent) {
        val str = StringBuilder("命令列表：\n")
        commands.forEach { (t, u) -> str.appendLine(".$t    ${u.handler.description}") }
        msg.reply(str.toString())
    }
}