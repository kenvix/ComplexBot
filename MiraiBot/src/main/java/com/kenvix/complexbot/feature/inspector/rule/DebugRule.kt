//--------------------------------------------------
// Class DebugRule
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.content

object DebugRule : InspectorRule {
    override val version: Int = 1
    override val description: String = "仅供调试。切勿在生产环境使用"
    override val punishReason: String = "Debug rule hit"
    override val name: String = "debug"

    override suspend fun onMessage(msg: MessageEvent): Boolean {
        return msg.message.content.startsWith(".inspectorDebugRule")
    }
}