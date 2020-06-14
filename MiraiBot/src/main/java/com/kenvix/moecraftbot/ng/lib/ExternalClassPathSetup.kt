package com.kenvix.moecraftbot.ng.lib

import java.io.File
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

object ExternalClassPathSetup {
    val loader: URLClassLoader by lazy { URLClassLoader.getSystemClassLoader() as URLClassLoader }
    private val urlClassLoaderMethod: Method by lazy {
        val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
        method.isAccessible = true

        method
    }

    fun addURL(url: URL) {
        urlClassLoaderMethod.invoke(loader, url)
    }

    fun addJarFile(file: File) {
        addURL(file.toURI().toURL())
    }

    fun addJarDirectory(dir: File) {
        dir.listFiles()?.forEach(::addJarFile)
    }
}