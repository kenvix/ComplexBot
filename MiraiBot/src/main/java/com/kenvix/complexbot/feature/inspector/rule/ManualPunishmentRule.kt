package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule

object ManualPunishmentRule : InspectorRule {
    override val version: Int = 1
    override val description: String = "回复某句消息来快速对该消息的发送者执行某个惩罚"
    override val punishReason: String = "Manual Punishment"
    override val name: String = "manualpunishment"
}