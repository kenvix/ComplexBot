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
    suspend fun getGroupOptions(groupId: Long): GroupOptions
    suspend fun saveGroupOptions(options: GroupOptions): UpdateResult
    suspend fun setGroupOptions(options: GroupOptions)
    suspend fun getAllGroupOptions(): List<GroupOptions>
}