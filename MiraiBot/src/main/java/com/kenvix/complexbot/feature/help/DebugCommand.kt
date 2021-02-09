package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.complexbot.callBridge
import com.kenvix.complexbot.reply
import com.kenvix.moecraftbot.ng.Defines
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isFriend
import net.mamoe.mirai.event.events.MessageEvent
import java.text.SimpleDateFormat
import java.util.*

object DebugCommand : BotCommandFeature {
    override val description: String
        get() = "调试命令"

    val formatter = SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS")

    override suspend fun onMessage(msg: MessageEvent) {
        val text = StringBuilder()
        text.appendLine("MoeNet Complex Bot v0.1")
        text.appendLine("Written by Kenvix | Github: kenvix/ComplexBot")
        text.appendLine("Powered by mamoe/mirai and MoeCraft Bot Framework")
        text.appendLine("${System.getProperty("java.vm.name")} ${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})")
        text.appendLine(callBridge.backendClient.aboutInfo)
        text.appendLine("Platform: ${System.getProperty("os.name")} | Kotlin ${KotlinVersion.CURRENT}")
        text.appendLine("Time: " + formatter.format(Date(System.currentTimeMillis())))

        val uptime = System.currentTimeMillis() - Defines.startedAt
        text.appendLine("Started at: " + formatter.format(Date(Defines.startedAt)))
        text.appendLine("UpTime ${uptime}ms")

        val total = Runtime.getRuntime().totalMemory().toInt() / 1024 / 1024
        val free = Runtime.getRuntime().freeMemory().toInt() / 1024 / 1024
        val max = (Runtime.getRuntime().maxMemory() / 1024 / 1024).toInt()
        text.appendLine("Memory：$free/$total MiB (Max $max MiB)")
        text.appendLine("===Sender Info===")
        text.append(msg.sender.run {
            when(this) {
                is Friend -> "Friend: $id($nick)\nAvatar: $avatarUrl"
                is Member -> "Member: $id($nick)\nAvatar: $avatarUrl\nSpecialTitle: $specialTitle | " +
                        "NameCard: $nameCard\nPermission: ${this.permission}" +
                        " | isFriend: ${this.isFriend} \nGroup ${group.id}(${group.name})"
                else -> "Unknown"
            }
        })

        msg.reply(text.toString())
    }
}