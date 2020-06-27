@file:JvmName("Utils")
package com.kenvix.moecraftbot.ng.lib

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*
import kotlin.math.ln
import kotlin.math.pow


const val CHAT_TYPE_IDLE = 0
const val CHAT_TYPE_AUTH = 0xa04

fun <T: Named> importNamedElementsIntoMap(target: MutableMap<String, T>, vararg things: T) {
    things.forEach { target[it.name] = it }
}

fun <T: Named> createNamedElementsMap(vararg things: T): Map<String, T> {
    return things.associateBy { it.name }
}

fun StringBuilder.replace(oldStr: String, newStr: String): StringBuilder {
    var index = this.indexOf(oldStr)
    if (index > -1 && oldStr != newStr) {
        var lastIndex: Int
        while (index > -1) {
            this.replace(index, index + oldStr.length, newStr)
            lastIndex = index + newStr.length
            index = this.indexOf(oldStr, lastIndex)
        }
    }
    return this
}

fun String.replacePlaceholders(placeholdersMap: Map<String, String>): String {
    val builder = StringBuilder(this)
    for ((key: String, value: String) in placeholdersMap) {
        builder.replace("#<$key>", value)
    }

    return builder.toString()
}

fun String.replacePlaceholders(placeholder: Pair<String, String>) = this.replacePlaceholders(mapOf(placeholder))

@JvmOverloads
fun Date.format(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(format).format(this)
}

@JvmOverloads
fun getHumanReadableByteSizeCount(bytes: Long, si: Boolean = false): String {
    val unit = if (si) 1000 else 1024
    if (bytes < unit) return "$bytes B"
    val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
    val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
    return String.format("%.1f %s%c", bytes / unit.toDouble().pow(exp.toDouble()), pre, if (si) 'b' else 'B')
}

fun getHumanReadableRemainTime(remaining: Long): String {
    var remainingTime = Duration.ofMillis(remaining)
    val days = remainingTime.toDays()
    remainingTime = remainingTime.minusDays(days)
    val hours = remainingTime.toHours()
    remainingTime = remainingTime.minusHours(hours)
    val minutes = remainingTime.toMinutes()
    remainingTime = remainingTime.minusMinutes(minutes)
    val seconds = remainingTime.seconds

    val result: StringBuilder = StringBuilder()
    if (days > 0) result.append("${days}d ")
    if (hours > 0) result.append("${hours}h ")
    if (minutes > 0) result.append("${minutes}m ")
    if (seconds > 0) result.append("${seconds}s")

    return result.toString()
}

fun Array<StackTraceElement>.getStringStackTrace(): String {
    val builder = StringBuilder()

    for (stackTrace in this) {
        builder.appendln("at $stackTrace")
    }

    return builder.toString()
}

fun Throwable.getStringStackTrace(): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.printStackTrace(PrintStream(byteArrayOutputStream))
    return byteArrayOutputStream.toString()
}

val Throwable.nameAndHashcode
    get() = "${this.javaClass.name}: ${this.hashCode()}"

val Boolean?.isTrue
    get() = this != null && this == true