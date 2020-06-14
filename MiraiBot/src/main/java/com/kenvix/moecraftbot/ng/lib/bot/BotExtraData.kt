//--------------------------------------------------
// Class BotExtraData
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.bot

import java.io.InputStream
import java.net.URI

data class BotExtraData (
    val users: List<BotUser>? = null,
    val photos: List<BotExtraPhoto>? = null, //TODO
    val file: List<BotExtraFile>? = null, //TODO
    val videos: List<BotExtraVideo>? = null //TODO
)

interface BotExtraFile {
    val id: String
    val fileStream: InputStream
    val fileURI: URI
    val size: Int
}

interface BotExtraVideo : BotExtraFile {
    val width: Int
    val height: Int
    val thumb: BotExtraPhoto?
}

interface BotExtraPhoto : BotExtraFile  {
    val width: Int
    val height: Int
}