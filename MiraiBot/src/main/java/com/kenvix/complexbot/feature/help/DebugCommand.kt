package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.CallBridge
import com.kenvix.complexbot.callBridge
import net.mamoe.mirai.message.MessageEvent
import java.text.SimpleDateFormat
import java.util.*

object DebugCommand : BotCommandFeature {
    override suspend fun onMessage(msg: MessageEvent) {
        val text = StringBuilder()
        text.appendln("MoeNet Complex Bot v0.1")
        text.appendln("Written by Kenvix")
        text.appendln("Powered by Mirai and MoeCraft Bot Framework")
        text.appendln("Java ${System.getProperty("java.version")}       Kotlin ${KotlinVersion.CURRENT}")
        text.appendln(callBridge.backendClient.aboutInfo)
        text.appendln("Platform: ${System.getProperty("os.name")}")
        text.appendln("Time: " + SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(Date(System.currentTimeMillis())))

        val total = Runtime.getRuntime().totalMemory().toInt() / 1024 / 1024
        val free = Runtime.getRuntime().freeMemory().toInt() / 1024 / 1024
        val max = (Runtime.getRuntime().maxMemory() / 1024 / 1024).toInt()
        text.appendln("Memory：$free/$total MiB (Max $max)")

        msg.reply(text.toString())
    }
}