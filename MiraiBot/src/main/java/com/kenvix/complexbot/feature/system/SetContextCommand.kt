package com.kenvix.complexbot.feature.system

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.content

object SetContextCommand : BotCommandFeature {
    override val description: String = "设置上下文群号，此命令可将会话强制切换到某个群聊上下文，之后所有命令在该上下文下执行（仅限私聊）"

    override suspend fun onMessage(msg: MessageEvent) {
        val commands = parseCommandFromMessage(msg.message.content)

    }
}