//--------------------------------------------------
// Class InspectorRule
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot.feature.inspector

import com.kenvix.moecraftbot.ng.lib.Named
import net.mamoe.mirai.message.MessageEvent
import java.lang.Exception

interface InspectorRule : Named {
    val version: Int
    val description: String
    val punishReason: String

    interface Actual : InspectorRule {
        /**
         * On message
         * @return A not null result stands for a certain rule matched and should punish sender
         * @throws Exception Once a exception was thrown, will NOT punish and log it
         */
        @Throws(Exception::class)
        suspend fun onMessage(msg: MessageEvent, relatedPlaceholders: List<Placeholder> = emptyList()): InspectorRule?
    }

    interface Placeholder : InspectorRule {
        val actualRule: Actual
    }
}