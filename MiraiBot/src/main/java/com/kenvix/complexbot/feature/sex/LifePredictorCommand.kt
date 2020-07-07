package com.kenvix.complexbot.feature.sex

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.content
import java.util.*

object LifePredictorCommand : BotCommandFeature {
    private val resultArray = arrayOf("大凶", "凶", "小凶", "凶多于吉", "吉多于凶", "小吉", "吉", "大吉")
    private val calendar = Calendar.getInstance()

    override suspend fun onMessage(msg: MessageEvent) {
        val command = parseCommandFromMessage(msg.message.content, false)
        val result = (msg.sender.id +
                    calendar.get(Calendar.DAY_OF_MONTH) +
                    calendar.get(Calendar.MONTH) +
                    calendar.get(Calendar.YEAR) +
                    command.firstArgument.hashCode()
                ).rem(resultArray.size).toInt()
        msg.reply("你好，${msg.sender.nick}\n所求事项：${command.firstArgument}\n结果：${resultArray[result]}")
    }
}