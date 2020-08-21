package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.callBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.content

object PSSisterAd : AbstractBayesBackendAd() {
    override val name: String = "pssisterad"
    override val version: Int = 1
    override val description: String = "PS 学姐和文字诈骗广告"
    override val punishReason: String = "是PS学姐或搞诈骗"

    override suspend fun onMessage(msg: MessageEvent): Boolean = withContext(Dispatchers.IO) {
        val sender = msg.sender as Member
        msg.message.content.let { text ->
            if (requiredMatchPattern.containsMatch(text)) {
                when (callBridge.backendClient.classificateTextMessage(text)) {
                    "pssisterad" -> true
                    "fraudad" -> true
                    "sellad" -> true
                    else -> false
                }
            } else {
                false
            }
        }
    }
}