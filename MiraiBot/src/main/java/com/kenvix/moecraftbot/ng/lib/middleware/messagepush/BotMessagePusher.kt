//--------------------------------------------------
// Class BotMessagePusher
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.middleware.messagepush

import com.kenvix.moecraftbot.ng.Defines
import com.kenvix.moecraftbot.ng.lib.api.APIResult
import com.kenvix.moecraftbot.ng.lib.bot.*
import com.kenvix.moecraftbot.ng.lib.middleware.BotMiddleware
import com.kenvix.utils.log.Logging
import com.kenvix.utils.log.info
import com.kenvix.utils.log.warning
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.UnauthorizedResponse

class BotMessagePusher : BotMiddleware, Logging {
    private lateinit var driverContext: AbstractDriver<*>
    private lateinit var botProvider: AbstractBotProvider<*>

    override fun onEnable(driverContext: AbstractDriver<*>) {
        this.driverContext = driverContext
        this.botProvider = driverContext.botProvider

        Defines.httpServer.routes {
            post("/api/pusher") { ctx ->
                onPushRequest(
                    ctx = ctx,
                    chatIdStr = ctx.formParam("chat") ?: throw BadRequestResponse("parameter chat cannot be empty"),
                    message = ctx.formParam("message"),
                    key = ctx.formParam("key") ?: "",
                    from = MessageFrom.getMessageFromFromString(ctx.formParam("from") ?: "private")
                )
            }

            get("/api/pusher") { ctx ->
                onPushRequest(
                    ctx = ctx,
                    chatIdStr = ctx.queryParam("chat") ?: throw BadRequestResponse("parameter chat cannot be empty"),
                    message = ctx.queryParam("message"),
                    key = ctx.queryParam("key") ?: "",
                    from = MessageFrom.getMessageFromFromString(ctx.queryParam("from") ?: "private")
                )
            }
        }
    }

    private fun onPushRequest(ctx: Context, chatIdStr: String, message: String?, key: String, from: MessageFrom) {
        try {
            val chatId = chatIdStr.toLong()

            if (from == MessageFrom.Unknown)
                throw IllegalArgumentException("parameter from is illegal")

            if (!Defines.systemOptions.messagePush.key.isNullOrBlank() && Defines.systemOptions.messagePush.key != key)
                throw UnauthorizedResponse("Key not match")

            val images = ctx.uploadedFiles("images")
            var imageExtra: MutableList<BotExtraPhoto>? = null
            if (images.isNotEmpty()) {
                imageExtra = mutableListOf()
                images.forEach { imageExtra.add(UploadPhoto(ctx, it)) }
            }

            var extraData: BotExtraData? = null
            if (imageExtra != null) {
                extraData = BotExtraData(photos = imageExtra)
            }

            val result: BotMessage = try {
                botProvider.sendMessage(chatId, message ?: "",
                    if (extraData == null) MessageType.Text else MessageType.Mixed, null, from, extraData)
            } catch (e: Exception) {
                logger.warning(e, "Send message failed")
                throw e
            }

            ctx.json(APIResult(0, "OK", SendMessageResult(result.id)))

        } catch (e: Exception) {
            when (e) {
                is NumberFormatException -> throw BadRequestResponse("chat id must be legal number")
                is IllegalArgumentException -> throw BadRequestResponse(e.message ?: "")
                is UnauthorizedResponse -> throw e
                else -> {
                    logger.warning(e)
                    throw InternalServerErrorResponse(e.message ?: "")
                }
            }
        }
    }

    private data class SendMessageResult (
        val id: Long
    )

    override fun getLogTag(): String = "BotMessagePusher"
}