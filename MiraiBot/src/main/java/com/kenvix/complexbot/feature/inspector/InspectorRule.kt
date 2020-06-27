//--------------------------------------------------
// Class InspectorRule
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot.feature.inspector

import com.kenvix.moecraftbot.ng.lib.Named
import net.mamoe.mirai.message.MessageEvent

interface InspectorRule : Named {
    val version: Int
    val description: String

    /**
     * On message
     * @return boolean Is rule matched and should punish sender
     */
    suspend fun onMessage(msg: MessageEvent): Boolean
}