//--------------------------------------------------
// Interface CallBridge
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot

import com.kenvix.android.utils.Coroutines
import com.kenvix.complexbot.rpc.thrift.BackendBridge
import com.kenvix.moecraftbot.ng.lib.ConfigManager
import com.mongodb.client.result.UpdateResult
import net.mamoe.mirai.contact.Contact

interface CallBridge {
    val backendClient: BackendBridge.Client
    val config: ComplexBotConfig
    fun getGroupOptions(groupId: Long): GroupOptions
    fun saveGroupOptions(options: GroupOptions): UpdateResult
    fun setGroupOptions(options: GroupOptions)
    fun getAllGroupOptions(): List<GroupOptions>
}