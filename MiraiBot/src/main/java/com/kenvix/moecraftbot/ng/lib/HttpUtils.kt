package com.kenvix.moecraftbot.ng.lib

import com.kenvix.moecraftbot.ng.Defines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

private val utilsLogger = LoggerFactory.getLogger("HttpUtils")!!

val uploadTempPath: Path by lazy {
    if (System.getProperty("java.io.tmpdir").isNullOrBlank()) {
        File("_temp").apply {
            if (this.exists())
                this.mkdirs()
        }
        System.setProperty("java.io.tmpdir", "_temp")
    }

    if (Defines.systemOptions.system.uploadTempDir.isNullOrBlank())
        Paths.get(System.getProperty("java.io.tmpdir"))
    else
        Paths.get(Defines.systemOptions.system.uploadTempDir)
}

suspend fun <R> File.useTempFile(then: (suspend (File) -> R))
        = withContext(Dispatchers.IO) {

    try {
        then(this@useTempFile)
    } catch (exception: Throwable) {
        kotlin.runCatching { this@useTempFile.delete() }
            .onFailure { warn("Delete temp file failed", it, utilsLogger) }

        throw exception
    }
}