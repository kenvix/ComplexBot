package com.kenvix.complexbot.feature.sex

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import kotlinx.coroutines.delay
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText

object RepeatCommand : BotCommandFeature {
    override val description: String = "复读某句话（只限管理员）"

    override suspend fun onMessage(msg: MessageEvent) {
        val command = parseCommandFromMessage(
            msg.message[PlainText.Key]?.content ?: "", true
        )

        if (command.firstArgumentOrNull.isNullOrBlank()) {
            msg.reply("用法：.repeat 复读次数 内容")
        } else {
            val num = command.firstArgument.toInt()
            if (num > 10) {
                msg.reply("不允许复读超过 10 次")
            } else {
                val content = MessageChainBuilder().apply {
                    add(msg.message[PlainText.Key]!!.content.run content@ {
                        substring( indexOf(command.firstArgument) + command.firstArgument.length)
                    })

                    addAll(msg.message.filterNot { it is PlainText })
                    addAll(msg.message.filterIsInstance<PlainText>().drop(1))
                }.build()

                (1 .. num).forEach { _ ->
                    delay(350)
                    msg.reply(content)
                }
            }
        }
    }
}