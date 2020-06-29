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
    override val punishReason: String
        get() = "疑似被盗号并发送了二维码黄赌毒广告，请勿相信"

    override suspend fun onMessage(msg: MessageEvent): Boolean {
        return false
    }
}