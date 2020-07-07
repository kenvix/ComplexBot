package com.kenvix.complexbot.feature.sex

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.content
import java.util.*
import kotlin.math.absoluteValue

object LifePredictorCommand : BotCommandFeature {
    private val resultArray = arrayOf("大凶", "凶", "小凶", "凶多于吉", "吉多于凶", "小吉", "吉", "大吉")
    private val calendar = Calendar.getInstance()
    override val description: String
        get() = "算卦"

    override suspend fun onMessage(msg: MessageEvent) {
        val command = parseCommandFromMessage(msg.message.content, false)
        val result =
                (msg.sender.id xor 0xDEAD_BEEF_CAFE xor
                    (calendar.get(Calendar.DAY_OF_MONTH) xor
                        calendar.get(Calendar.MONTH) xor
                        calendar.get(Calendar.YEAR) xor
                        command.firstArgument.hashCode()
                    ).toLong()
                ).rem(resultArray.size).toInt().absoluteValue
        msg.reply(MessageChainBuilder().apply {
            add("你好，")

            if (msg.sender is Member)
                add(At(msg.sender as Member))
            else
                add(msg.sender.nick)

            add("\n所求事项：${command.firstArgument}\n结果：${resultArray[result]}")
        }.build())
    }
}