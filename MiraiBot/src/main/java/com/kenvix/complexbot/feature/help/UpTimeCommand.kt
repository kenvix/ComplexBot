package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.moecraftbot.ng.Defines
import net.mamoe.mirai.message.MessageEvent
import java.time.LocalTime
import java.util.*

object UpTimeCommand : BotCommandFeature {
    override val description: String
        get() = "运行时间"

    override suspend fun onMessage(msg: MessageEvent) {
        val text = StringBuilder()

        text.appendln("当前时间: " + DebugCommand.formatter.format(Date(System.currentTimeMillis())))
        val uptime = System.currentTimeMillis() - Defines.startedAt
        text.appendln("启动于: " + DebugCommand.formatter.format(Date(Defines.startedAt)))


        val upDays = uptime / 86400_000
        val timeOfDay = LocalTime.ofSecondOfDay((uptime / 1000) % 86400)
        text.append("已运行时间 ${uptime/1000}s : $upDays:$timeOfDay")
        msg.reply(text.toString())
    }
}