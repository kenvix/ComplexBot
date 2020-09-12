package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule

object NonWellKnownUrl : InspectorRule {
    override val version: Int = 1
    override val description: String = "不知名的链接"
    override val punishReason: String = "发送了不常见的网站地址"
    override val name: String = "nonfamousurl"


}