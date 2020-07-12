package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.CallBridge
import net.mamoe.mirai.message.MessageEvent

object HelpCommand : BotCommandFeature {
    override val description: String
        get() = "帮助"

    override suspend fun onMessage(msg: MessageEvent) {
        msg.reply(StringBuilder("MoeNet Complex Bot v0.1\n").apply {
            appendln("所有命令前需要加点“.”，命令参数用空格分隔")
            appendln(".help  查看帮助")
            appendln(".listcommands 列出命令列表")
        }.toString())
    }
}