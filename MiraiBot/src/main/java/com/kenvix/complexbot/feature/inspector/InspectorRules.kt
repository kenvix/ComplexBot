package com.kenvix.complexbot.feature.inspector

import com.kenvix.complexbot.feature.inspector.rule.DocumentAd
import com.kenvix.complexbot.feature.inspector.rule.PSSisterAd
import com.kenvix.complexbot.feature.inspector.rule.PhotoQrCodeAd
import com.kenvix.moecraftbot.ng.lib.createNamedElementsMap

val inspectorRules = createNamedElementsMap(DocumentAd, PhotoQrCodeAd, PSSisterAd)