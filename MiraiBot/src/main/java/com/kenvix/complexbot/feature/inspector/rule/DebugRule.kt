//--------------------------------------------------
// Class DebugRule
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content

object DebugRule : InspectorRule.Actual {
    override val version: Int = 1
    override val description: String = "仅供调试。切勿在生产环境使用"
    override val punishReason: String = "Debug rule hit"
    override val name: String = "debug"

    override suspend fun onMessage(msg: MessageEvent, relatedPlaceholders: List<InspectorRule.Placeholder>): InspectorRule? {
        return if (msg.message.content.startsWith(".inspectorDebugRule")) this else null
    }
}