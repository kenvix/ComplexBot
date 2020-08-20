package com.kenvix.complexbot.feature.sex

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.*
import java.util.*
import kotlin.math.absoluteValue

object LifePredictorCommand : BotCommandFeature {
    private val resultArray = arrayOf("大凶", "凶", "小凶", "凶多于吉", "吉多于凶", "小吉", "吉", "大吉")
    override val description: String
        get() = "算卦"
    private val offsetStr = System.getProperties()["complexbot.lifepredictor.offsetstr"] ?: "M O E C R A F T"
    private val offsetStrHash = offsetStr.hashCode()

    override suspend fun onMessage(msg: MessageEvent) {
        val command = parseCommandFromMessage(msg.message.content, false)
        if (command.firstArgumentOrNull.isNullOrBlank()) {
            msg.reply("来算一卦吧！示例食用方法：\n。算卦 写代码")
        } else if (!msg.message.none { it is Image || it is RichMessage }) {
            msg.reply("只有言语才有算卦的意义哦")
        } else {
            val result = (
                    (msg.sender.id shl 0xC) xor 0xDEAD_BEEFL xor (System.currentTimeMillis() shr 27) xor
                            (command.firstArgument.hashCode() xor offsetStrHash).toLong()
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
}