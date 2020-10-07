//--------------------------------------------------
// Interface CallBridge
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot

import com.kenvix.complexbot.rpc.thrift.BackendBridge
import com.mongodb.client.result.UpdateResult
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group

interface CallBridge {
    val backendClient: BackendBridge.Client
    val config: ComplexBotConfig
    val context: Map<Long, Context>
    val driver: ComplexBotDriver
    fun getGroupOptions(groupId: Long): GroupOptions
    fun saveGroupOptions(options: GroupOptions): UpdateResult
    fun setGroupOptions(options: GroupOptions)
    fun getAllGroupOptions(): List<GroupOptions>
}

data class Context(
    val context: Contact
) {
    val isGroup
        get() = context is Group
    val isPrivate
        get() = context is Friend

    val group
        get() = context as Group
}