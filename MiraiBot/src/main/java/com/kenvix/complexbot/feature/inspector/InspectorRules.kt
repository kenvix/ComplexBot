package com.kenvix.complexbot.feature.inspector

import com.kenvix.complexbot.feature.inspector.rule.*
import com.kenvix.moecraftbot.ng.lib.createNamedElementsMap

val inspectorRules = createNamedElementsMap(
        DocumentAd,
        PhotoQrCodeAd,
        PSSisterAd,
        FraudAd,
        SellAd,
        UselessApp,
        DebugRule
)