//--------------------------------------------------
// Class UploadPhoto
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.middleware.messagepush

import com.kenvix.moecraftbot.ng.lib.bot.BotExtraPhoto
import io.javalin.http.Context
import io.javalin.http.UploadedFile
import java.io.InputStream
import java.net.URI

class UploadPhoto(val ctx: Context, val upload: UploadedFile) : BotExtraPhoto {
    override val width: Int
        get() = 0
    override val height: Int
        get() = 0
    override val id: String
        get() = upload.hashCode().toString()
    override val fileStream: InputStream
        get() = upload.content
    override val fileURI: URI
        get() = URI("pushupload://${ctx.hashCode()}/${upload.filename}")
    override val size: Int
        get() = upload.size.toInt()
}