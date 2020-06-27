package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import net.mamoe.mirai.message.MessageEvent

object PhotoQrCodeAd : InspectorRule {
    override val name: String
        get() = "photoqrcodead"
    override val version: Int
        get() = 1
    override val description: String
        get() = "非法二维码广告"

    override suspend fun onMessage(msg: MessageEvent): Boolean {
        TODO("Not yet implemented")
    }
}