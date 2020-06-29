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

    /**
     * On message
     * @return boolean Is rule matched and should punish sender
     * @throws Exception Once a exception was thrown, will NOT punish
     */
    @Throws(Exception::class)
    suspend fun onMessage(msg: MessageEvent): Boolean
}