//--------------------------------------------------
// Interface CallBridge
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot

import com.kenvix.android.utils.Coroutines
import com.kenvix.complexbot.rpc.thrift.BackendBridge
import com.kenvix.moecraftbot.ng.lib.ConfigManager

interface CallBridge {
    val backendClient: BackendBridge.Client
    val config: ComplexBotConfig
}