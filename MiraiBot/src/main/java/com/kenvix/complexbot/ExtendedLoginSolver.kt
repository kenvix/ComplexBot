//--------------------------------------------------
// Class ExtendedLoginSolver
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot

import com.kenvix.utils.log.Logging
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.LoginSolver
import java.nio.ByteBuffer
import java.util.*

class ExtendedLoginSolver(private val callBridge: CallBridge) : LoginSolver(), Logging {
    companion object {
        const val MaxContinuousFailCountPerMinute = 10
    }

    private val standardLoginSolver by lazy { LoginSolver.Default }
    private val continuousFailCounter = WeakHashMap<Bot, ContinuousFail>()

    private data class ContinuousFail(val beginTime: Long, var count: Int)

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        val fail = continuousFailCounter[bot]
        logger.debug("Solving captcha ...")

        if (fail != null) {
            if (System.currentTimeMillis() - fail.beginTime <= 60_000)
                fail.count++
            else
                continuousFailCounter.remove(bot)

            if (fail.count >= MaxContinuousFailCountPerMinute) {
                logger.info("Captcha breaker keep failing, please manual solve captcha")
                return standardLoginSolver.onSolvePicCaptcha(bot, data)
            }
        }

        continuousFailCounter[bot] = ContinuousFail(System.currentTimeMillis(), 0)
        return callBridge.backendClient.parseCaptchaFromBinary(ByteBuffer.wrap(data)).also {
            logger.trace("Captcha breaker recv: $it")
        }
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        return standardLoginSolver.onSolveSliderCaptcha(bot, url)
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        return standardLoginSolver.onSolveUnsafeDeviceLoginVerify(bot, url)
    }
}