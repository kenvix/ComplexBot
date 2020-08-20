package com.kenvix.complexbot.feature.sex

import com.kenvix.complexbot.BotCommandFeature
import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.*
import java.util.*
import kotlin.math.absoluteValue

object LifePredictorCommand : BotCommandFeature {
    private val resultArray = arrayOf("大凶", "凶", "小凶", "凶多于吉", "吉多于凶", "小吉", "吉", "大吉")
    override val description: String
        get() = "算卦"
    private val offsetStr = System.getProperties()["complexbot.lifepredictor.offsetstr"] ?: "M O E C R A F T"
    private val offsetStrHash = offsetStr.hashCode()
    
    private val starGods = arrayOf("天刑", "朱雀", "金匮", "天德", "白虎", "玉堂", "截路空亡", "截路", "司命", "勾陈", "青龙", "明堂")
    private val fiveElements = arrayOf("涧下水", "涧下水", "城头土", "城头土", "白腊金", "白腊金", "杨柳木", "杨柳木", "井泉水", "井泉水", "房上土", "房上土")
    private val shaFangs = arrayOf("煞南", "煞东", "煞北", "煞西", "煞南", "煞东", "煞北", "煞西", "煞南", "煞东", "煞北", "煞西")
    private val goodGods = arrayOf("贵人 喜神", "帝旺 金匮 福德", "三合 大进 日禄 宝光", "天福 五合",
            "玉堂 驿马 少微", "六合 长生", "右弼", "罗纹 交贵", "比肩 中兵", "喜神 青龙", "三合 福星 明堂 国印")
    private val badGods = arrayOf("天兵 天刑", "日破 日刑 朱雀", "六戊", "白虎 地兵 旬空", "不遇", "狗食 路空",
            "日建 元武 路空", "天贼 大退", "勾陈", "天兵 日刑")

    override suspend fun onMessage(msg: MessageEvent) {
        val command = parseCommandFromMessage(msg.message.content, false)
        if (command.firstArgumentOrNull.isNullOrBlank()) {
            msg.reply("来算一卦吧！示例食用方法：\n。算卦 写代码")
        } else if (!msg.message.none { it is Image || it is RichMessage }) {
            msg.reply("只有言语才有算卦的意义哦")
        } else {
            val code = msg.sender.id xor 0xDEAD_BEEFL xor (System.currentTimeMillis() shr 27) xor
                    ((command.firstArgument.hashCode() xor offsetStrHash).toLong() shl 13)

            val result = code.rem(resultArray.size).toInt().absoluteValue
            val starGod: Int      = ((code and 0x0000F0) % starGods.size).absoluteValue.toInt()
            val fiveElement: Int  = ((code and 0x000F00) % fiveElements.size).absoluteValue.toInt()
            val shaFang: Int      = ((code and 0x00F000) % shaFangs.size).absoluteValue.toInt()
            val goodGod: Int      = ((code and 0x0F0000) % goodGods.size).absoluteValue.toInt()
            val badGod: Int       = ((code and 0xF00000) % badGods.size).absoluteValue.toInt()

            msg.reply(MessageChainBuilder().apply {
                add("你好，")

                if (msg.sender is Member)
                    add(At(msg.sender as Member))
                else
                    add(msg.sender.nick)

                add("\n所求事项：${command.firstArgument}\n结果：${resultArray[result]}")
                add("\n${starGods[starGod]} | ${fiveElements[fiveElement]} | ${shaFangs[shaFang]} | ${goodGods[goodGod]} | ${badGods[badGod]}")
            }.build())
        }
    }
}