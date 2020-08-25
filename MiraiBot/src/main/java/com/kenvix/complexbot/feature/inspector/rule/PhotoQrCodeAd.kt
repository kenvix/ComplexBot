package com.kenvix.complexbot.feature.inspector.rule

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.detector.Detector
import com.kenvix.complexbot.feature.inspector.InspectorRule
import com.kenvix.complexbot.feature.inspector.InspectorStatisticUtils
import com.kenvix.moecraftbot.ng.Defines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.queryUrl
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import javax.imageio.ImageIO


object PhotoQrCodeAd : InspectorRule {
    override val name: String = "photoqrcodead"
    override val version: Int = 1
    override val description: String = "二维码广告。（此策略较为粗糙，慎用）"
    override val punishReason: String = "疑似发送了不受欢迎的二维码广告。（此策略较为粗糙，若误报请说明）"

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val detectHints: Map<DecodeHintType, Any> = mapOf(
        DecodeHintType.TRY_HARDER to java.lang.Boolean.TRUE
    )

    @Suppress("SimplifyBooleanWithConstants")
    override suspend fun onMessage(msg: MessageEvent): Boolean {
        val sender = msg.sender as Member
        val images = msg.message.filterIsInstance<Image>()

        if (images.isNotEmpty()) {
            val stat = InspectorStatisticUtils.getStat(sender.group.id).stats[sender.id]

            if (stat == null ||
                stat.countLegal <= 8L ||
                (stat.countIllegal >= 4L && stat.counts[InspectorStatisticUtils.todayKey] ?: 0 <= 1)
            ) { //Assume ad sender has low activity
                return images.asFlow().map {
                    detectImage(it)
                }.firstOrNull {
                    it == true
                } ?: false
            }
        }

        return false
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun detectImage(image: Image) = withContext(Dispatchers.IO) {
        val url = image.queryUrl()
        val request = downloadCallOf(url)
        logger.trace("Trying to detect QRCode AD form url $url")

        request.execute().use { response ->
            response.body?.run imageReq@ {
                val bufferedImage = ImageIO.read(this@imageReq.byteStream())

                if (bufferedImage != null)
                    detectQRCode(bufferedImage) || detectQRCode(bufferedImage.flip())
                else
                    false
            } ?: false
        }
    }

    private suspend fun detectQRCode(bufferedImage: BufferedImage) = withContext(Dispatchers.IO) {
        try {
            val binaryBitmap = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(bufferedImage)))
            Detector(binaryBitmap.blackMatrix).detect(detectHints)
            true
        } catch (e: Throwable) {
            logger.debug("QRCode ad detector returned false: $e")
            false
        }
    }

    fun BufferedImage.flip(): BufferedImage {
        for (i in 0 until width) for (j in 0 until height / 2) {
            val tmp = getRGB(i, j)
            setRGB(i, j, getRGB(i, height - j - 1))
            setRGB(i, height - j - 1, tmp)
        }

        return this
    }

    private fun downloadCallOf(url: String) =
        Request.Builder()
            .url(url)
            .build()
            .run {
                Defines.okHttpClient.newCall(this)
            }
}