package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule

object PSSisterAd : InspectorRule.Placeholder {
    override val name: String = "pssisterad"
    override val actualRule: InspectorRule.Actual = BayesBasedAd
    override val version: Int = 1
    override val description: String = "PS 学姐和和推销广告"
    override val punishReason: String = "是PS学姐或搞推销"
}

object FraudAd : InspectorRule.Placeholder {
    override val name: String = "fraudad"
    override val actualRule: InspectorRule.Actual = BayesBasedAd
    override val version: Int = 1
    override val description: String = "恶意诈骗广告"
    override val punishReason: String = "恶意诈骗"
}

object SellAd : InspectorRule.Placeholder {
    override val name: String = "sellad"
    override val actualRule: InspectorRule.Actual = BayesBasedAd
    override val version: Int = 1
    override val description: String = "物品交易广告"
    override val punishReason: String = "发送物品交易广告"
}