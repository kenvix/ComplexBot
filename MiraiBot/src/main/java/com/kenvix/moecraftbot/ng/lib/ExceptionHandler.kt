package com.kenvix.moecraftbot.ng.lib

import com.kenvix.utils.event.eventSetOf
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

object ExceptionHandler : Thread.UncaughtExceptionHandler {
    val logger = LoggerFactory.getLogger("ExceptionHandler")
    val handlers = eventSetOf<WrappedException>()

    fun registerGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        logger.error("Exception in thread ${t.name}", e)
        handle(e, Level.ERROR, t)
    }

    fun handle(exception: Throwable, importance: Level = Level.ERROR, thread: Thread = Thread.currentThread()) {
        if (handlers.isNotEmpty())
            handle(WrappedException(exception, importance, thread))
    }

    fun handle(exception: WrappedException) {
        handlers(exception)
    }

    data class WrappedException(
        val exception: Throwable,
        val importance: Level = Level.ERROR,
        val thread: Thread = Thread.currentThread()
    )
}

fun error(message: String, exception: Throwable, logger: Logger = ExceptionHandler.logger) {
    logger.error(message, exception)
    ExceptionHandler.handle(exception, Level.ERROR)
}

fun warn(message: String, exception: Throwable, logger: Logger = ExceptionHandler.logger) {
    logger.warn(message, exception)
    ExceptionHandler.handle(exception, Level.WARN)
}

fun info(message: String, exception: Throwable, logger: Logger = ExceptionHandler.logger) {
    logger.info(message, exception)
    ExceptionHandler.handle(exception, Level.INFO)
}

fun trace(message: String, exception: Throwable, logger: Logger = ExceptionHandler.logger) {
    logger.trace(message, exception)
    ExceptionHandler.handle(exception, Level.TRACE)
}

fun debug(message: String, exception: Throwable, logger: Logger = ExceptionHandler.logger) {
    logger.debug(message, exception)
    ExceptionHandler.handle(exception, Level.DEBUG)
}