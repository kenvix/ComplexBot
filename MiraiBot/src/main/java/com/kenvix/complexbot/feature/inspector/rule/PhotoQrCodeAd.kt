package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import com.kenvix.complexbot.feature.inspector.InspectorStatisticUtils
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.FlashImage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.queryUrl

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
        return msg.message.any { message ->
            when (message) {
                is Image -> verifyImageMessage(msg, message)
                is FlashImage -> verifyImageMessage(msg, message.image)
                else -> false
            }
        }
    }

    private suspend fun verifyImageMessage(msgEvent: MessageEvent, image: Image): Boolean {
        val sender = msgEvent.sender as Member
        val stat = InspectorStatisticUtils.getStat(sender.group.id).stats[sender.id]
        if (stat == null || stat.countLegal <= 1L) { //Assume ad sender has low activity

        }

        return false
    }
}