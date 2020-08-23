@file:JvmName("Utils")
package com.kenvix.moecraftbot.ng.lib

import com.google.common.cache.CacheLoader
import com.kenvix.complexbot.GroupOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.ln
import kotlin.math.pow


const val CHAT_TYPE_IDLE = 0
const val CHAT_TYPE_AUTH = 0xa04

typealias DateTime = OffsetDateTime
fun DateTime.toEpochMilli() = toInstant().toEpochMilli()

fun Int.hasFlag(flag: Int): Boolean = (this and flag) != 0
fun Long.hasFlag(flag: Long): Boolean = (this and flag) != 0L

/**
 * 返回置 Flag 之后的值
 */
fun Int.flaggedOf(flag: Int) = this or flag

/**
 * 返回置 Flag 之后的值
 */
fun Long.flaggedOf(flag: Long): Long = this or flag

/**
 * 返回清除 Flag 之后的值
 */
fun Int.unflaggedOf(flag: Int): Int = this and (flag.inv())

/**
 * 返回清除 Flag 之后的值
 */
fun Long.unflaggedOf(flag: Long): Long = this and (flag.inv())

class ExtendedThreadLocal<T>(inline val getter: (() -> T)) : ThreadLocal<T>() {
    override fun initialValue(): T {
        return getter()
    }

    operator fun invoke() = get()!!
    override fun get(): T = super.get()!!
}

class ExtendedInheritableThreadLocal<T>(inline val getter: (() -> T)) : InheritableThreadLocal<T>() {
    override fun initialValue(): T {
        return getter()
    }

    operator fun invoke() = get()!!
    override fun get(): T = super.get()!!
}

fun <T> threadLocal(getter: (() -> T)): ExtendedThreadLocal<T> {
    return ExtendedThreadLocal(getter)
}

fun <T> inheritableThreadLocal(getter: (() -> T)): ExtendedInheritableThreadLocal<T> {
    return ExtendedInheritableThreadLocal(getter)
}

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

inline fun <T : Any, U : Collection<T>> U?.ifNotNullOrEmpty(then: ((U) -> Unit)) {
    if (this != null && this.isNotEmpty())
        then(this)
}

fun String.replacePlaceholders(placeholdersMap: Map<String, String>): String {
    val builder = StringBuilder(this)
    for ((key: String, value: String) in placeholdersMap) {
        builder.replace("#<$key>", value)
    }

    return builder.toString()
}

fun String.replacePlaceholders(placeholder: Pair<String, String>) = this.replacePlaceholders(mapOf(placeholder))


private val dateDefaultFormatter = threadLocal {
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
}

private val dateMilliFormatter = threadLocal {
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
}

fun Date.format(): String = dateDefaultFormatter().format(this)
fun Date.formatMilli(): String = dateMilliFormatter().format(this)

fun Date.toLocalDate() = toInstant().atZone(ZoneId.systemDefault()).toLocalDate()!!
fun Date.toLocalTime() = toInstant().atZone(ZoneId.systemDefault()).toLocalTime()!!
fun Date.toLocalDateTime() = toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()!!

private val instantDefaultFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss")
    .withZone(ZoneId.systemDefault())
private val instantMilliFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    .withZone(ZoneId.systemDefault())
private val instantNanosFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS")
    .withZone(ZoneId.systemDefault())
private val instantNormalizedFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'")
    .withZone(ZoneId.systemDefault())

fun Instant.format() = instantDefaultFormatter.format(this)!!
fun Instant.formatMilli() = instantMilliFormatter.format(this)!!
fun Instant.formatNanos() = instantNanosFormatter.format(this)!!
fun Instant.formatNormalized() = instantNormalizedFormatter.format(this)!!

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
        builder.appendLine("at $stackTrace")
    }

    return builder.toString()
}


inline fun stringPrintStream(printStream: ((PrintStream) -> Unit)): String {
    return ByteArrayOutputStream().use { b ->
        PrintStream(b).use { p ->
            printStream(p)
        }

        b.toByteArray().toString(Charsets.UTF_8)
    }
}

inline fun stringBuilder(next: ((StringBuilder) -> Unit)): String {
    val builder = StringBuilder()
    next(builder)

    return builder.toString()
}

inline fun <T, R> T?.ifNotNull(then: ((T) -> R?)): R? {
    if (this != null)
        return then(this)

    return null
}

inline fun <K, V> cacheLoader(crossinline loader: ((K) -> V)) = object : CacheLoader<K, V>() {
    override fun load(key: K): V = loader(key)
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

inline fun <reified E: Enum<E>> E.next(): E {
    val values = enumValues<E>()
    val nextOrdinal = (ordinal + 1) % values.size
    return values[nextOrdinal]
}

/**
 * Creates a [Flow] instance that wraps the original map returning its entries when being iterated.
 */
fun <K, V> Map<out K, V>.asFlow(): Flow<Map.Entry<K, V>> = flow {
    forEach {
        emit(it)
    }
}