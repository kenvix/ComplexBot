package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import com.kenvix.complexbot.feature.inspector.InspectorStatisticUtils
import com.kenvix.moecraftbot.ng.Defines
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.*

object PhotoQrCodeAd : InspectorRule {
    override val name: String = "photoqrcodead"
    override val version: Int = 1
    override val description: String = "非法二维码广告"
    override val punishReason: String = "疑似被盗号并发送了二维码黄赌毒广告，请勿相信"

    override suspend fun onMessage(msg: MessageEvent): Boolean {
        val sender = msg.sender as Member
        val images = msg.message.filterIsInstance<Image>()

        if (images.isNotEmpty()) {
            val stat = InspectorStatisticUtils.getStat(sender.group.id).stats[sender.id]
            if (stat == null || stat.countLegal <= 8L) { //Assume ad sender has low activity
                if (!msg.message.none { it is PlainText }
                    && !AbstractBayesBackendAd.requiredMatchPattern.containsMatch(msg.message.content)
                    && stat?.countLegal ?: 0 >= 3L) {
                    return true
                }

                for (image in images) {
                    val url = image.queryUrl()

                }
            }
        }

        return false
    }
}