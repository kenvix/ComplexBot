package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import net.mamoe.mirai.event.events.MessageEvent

object HelpCommand : BotCommandFeature {
    override val description: String
        get() = "帮助"

    override suspend fun onMessage(msg: MessageEvent) {
        msg.reply(StringBuilder("MoeNet Complex Bot v0.1\n").apply {
            appendLine("所有命令前需要加点“.”，命令参数用空格分隔")
            appendLine(".help  查看帮助")
            appendLine(".listcommands 列出命令列表")
        }.toString())
    }
}