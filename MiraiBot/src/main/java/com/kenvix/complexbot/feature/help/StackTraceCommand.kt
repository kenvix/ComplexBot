package com.kenvix.complexbot.feature.help

import com.kenvix.complexbot.BotCommandFeature
import net.mamoe.mirai.event.events.MessageEvent

object StackTraceCommand : BotCommandFeature {
    override val description: String = "最近的运行失败的堆栈跟踪（后面跟群号或报告序号，默认为当前会话）"

    override suspend fun onMessage(msg: MessageEvent) {
        TODO("Not yet implemented")
    }
}