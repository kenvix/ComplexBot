package com.kenvix.moecraftbot.ng

import com.google.gson.GsonBuilder
import com.kenvix.moecraftbot.ng.lib.ConfigManager
import com.kenvix.moecraftbot.ng.lib.ExternalClassPathSetup
import com.kenvix.moecraftbot.ng.lib.SystemOptions
import com.kenvix.moecraftbot.ng.lib.api.APIException
import com.kenvix.moecraftbot.ng.lib.api.APIResult
import com.kenvix.moecraftbot.ng.lib.bot.AbstractBotProvider
import com.kenvix.moecraftbot.ng.lib.bot.AbstractDriver
import com.kenvix.moecraftbot.ng.lib.bot.DriverFeature
import com.kenvix.moecraftbot.ng.lib.exception.InvalidConfigException
import com.kenvix.utils.log.LogSettings
import com.kenvix.utils.log.Logging
import com.kenvix.utils.log.severe
import io.javalin.Javalin
import io.javalin.http.HttpResponseException
import io.javalin.plugin.json.FromJsonMapper
import io.javalin.plugin.json.JavalinJson
import io.javalin.plugin.json.ToJsonMapper
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.apache.commons.cli.CommandLine
import org.apache.commons.dbcp2.BasicDataSource
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import java.io.File
import java.io.FileNotFoundException
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


object Defines : Logging {
    override fun getLogTag(): String = "AppDefines"

    @JvmStatic
    lateinit var baseConfigPath: String
        private set

    @JvmStatic
    lateinit var activeDriver: AbstractDriver<*>
        private set

    @JvmStatic
    lateinit var activeBotProvider: AbstractBotProvider<*>
        private set

    @JvmStatic
    lateinit var appCmds: CommandLine
        private set

    @JvmStatic
    val predefinedDriverNameMap = mapOf<String, String>(
            "n3ro" to "com.kenvix.moecraftbot.ng.driver.n3ro.N3roDriver",
            "example" to "com.kenvix.moecraftbot.ng.driver.example.ExampleDriver"
    )

    @JvmStatic
    val predefinedBotProviderNameMap = mapOf<String, String>(
            "telegram" to "com.kenvix.moecraftbot.ng.bot.provider.telegram.TelegramBot",
            "coolq" to "com.kenvix.moecraftbot.ng.bot.provider.coolq.CoolqBot",
            "mirai" to "com.kenvix.moecraftbot.ng.bot.provider.mirai.MiraiBot"
    )

    lateinit var driverThread: Thread
        private set

    lateinit var botProviderThread: Thread
        private set

    @JvmStatic
    lateinit var systemOptions: SystemOptions
        private set

    lateinit var cacheDirectory: File
        private set

    internal lateinit var dslContext: DSLContext
        private set

    internal lateinit var jooqConfiguration: Configuration
        private set

    internal lateinit var dataSource: BasicDataSource
        private set

    lateinit var okHttpClient: OkHttpClient
        private set

    lateinit var proxy: Proxy
        private set

    lateinit var httpServer: Javalin
        private set

    @JvmStatic
    lateinit var cachedThreadPool: ExecutorService
        private set

    private val loadLock = java.lang.Object()
    private val consoleInputHandlers: MutableList<ConsoleReadSupported> = LinkedList()

    internal fun setupSystem(appCmds: CommandLine) {
        logger.finest("Application Setup")

        this.appCmds = appCmds
        baseConfigPath = appCmds.getOptionValue('c', "config")

        if (!baseConfigPath.endsWith('/'))
            baseConfigPath += "/"

        run {
            val configDirFile = Paths.get(baseConfigPath).resolve("config").toFile()
            if (!configDirFile.exists())
                configDirFile.mkdirs()
        }

        val pluginDir = File("plugins")
        if (!pluginDir.exists())
            pluginDir.mkdirs()

        val pluginLibDir = pluginDir.toPath().resolve("libs").toFile()
        if (!pluginLibDir.exists())
            pluginLibDir.mkdirs()

        logger.fine("Loading plugins from ${pluginDir.absolutePath}")
        ExternalClassPathSetup.addJarDirectory(pluginDir)
        ExternalClassPathSetup.addJarDirectory(pluginLibDir)

        //Load bot driver
        val driverName = appCmds.getOptionValue('d') ?: throw InvalidConfigException("Driver full class name or predefined required")

        val driverClass = if (predefinedDriverNameMap.containsKey(driverName)) {
            Class.forName(predefinedDriverNameMap[driverName.toLowerCase()], true, ExternalClassPathSetup.loader)
        } else {
            Class.forName(driverName, true, ExternalClassPathSetup.loader)
        }

        activeDriver = driverClass.newInstance() as AbstractDriver<*>
        if (activeDriver.driverFeatures.contains(DriverFeature.ReadSystemConsoleInput))
            consoleInputHandlers.add(activeDriver)

        //Load bot provider
        val providerName = appCmds.getOptionValue('p', "telegram")

        val botProviderClass = if (predefinedBotProviderNameMap.containsKey(providerName)) {
            Class.forName(predefinedBotProviderNameMap[providerName.toLowerCase()], true, ExternalClassPathSetup.loader)
        } else {
            Class.forName(providerName, true, ExternalClassPathSetup.loader)
        }

        activeBotProvider = botProviderClass.newInstance() as AbstractBotProvider<*>

        if (activeBotProvider.providerOptions and AbstractBotProvider.OPTION_REDIRECT_STDIN != 0)
            consoleInputHandlers.add(activeBotProvider)

        //Load system (2)
        systemOptions = ConfigManager.getConfigManager("system", SystemOptions::class.java).content
        cachedThreadPool = ThreadPoolExecutor(1, systemOptions.system.threadPoolMaxSize,
            60L, TimeUnit.SECONDS, SynchronousQueue<Runnable>())
        cacheDirectory = File("cache")

    }

    internal fun setupDatabase() {
        System.getProperties().setProperty("org.jooq.no-logo", "true")
        dataSource = BasicDataSource()

        when(systemOptions.database.type.toLowerCase()) {
            "sqlite" -> {
                val databaseFileName = if (systemOptions.database.host.isNullOrBlank()) "database.sqlite3" else systemOptions.database.host
                val databaseFile = Paths.get(baseConfigPath).resolve(databaseFileName).toFile()
                val jdbcUriString = "jdbc:sqlite:${databaseFile.canonicalPath}"

                if (!databaseFile.exists()) {
                    logger.fine("Create new database file $databaseFileName")

                    val defaultConfigFileStream = this.javaClass.classLoader.getResourceAsStream(databaseFileName)
                        ?: throw FileNotFoundException("No default database file $databaseFileName found")

                    defaultConfigFileStream.copyTo(databaseFile.outputStream())
                }

                //dataSource.driverClassName = "org.xerial.sqlite-jdbc"
                dataSource.url = jdbcUriString

                dslContext = DSL.using(dataSource, SQLDialect.SQLITE)
                jooqConfiguration = DefaultConfiguration().set(dataSource).set(SQLDialect.SQLITE)
            }

            "mysql" -> {
                val param = "useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&autoReconnectForPools=true&serverTimezone=GMT%2B8"
                val url = "jdbc:mysql://${systemOptions.database.host}:${systemOptions.database.port}/${systemOptions.database.name}?$param"

                //dataSource.driverClassName = "mysql.mysql-connector-java"
                dataSource.url = url
                dataSource.username = systemOptions.database.user

                if (systemOptions.database.needAuth)
                    dataSource.password = systemOptions.database.password

                dslContext = DSL.using(dataSource, SQLDialect.MYSQL)
                jooqConfiguration = DefaultConfiguration().set(dataSource).set(SQLDialect.MYSQL)
            }

            "none" -> {
                logger.warning("Warning: No database enabled. If any modules using database was loaded, unexpected errors may occurs.")
            }

            else -> throw InvalidConfigException("Invalid database type ${systemOptions.database.type}")
        }

        dataSource.initialSize = 1
        dataSource.minIdle = 1
        dataSource.maxIdle = 3

        logger.finer("Database ready: ${systemOptions.database.type}://${systemOptions.database.host}")
    }

    internal fun setupNetwork() {
        if (systemOptions.proxy.enable) {
            logger.fine("Proxy enabled: ${systemOptions.proxy.type}://${systemOptions.proxy.host}:${systemOptions.proxy.port}")

            proxy = Proxy(
                when (systemOptions.proxy.type) {
                    SystemOptions.Proxy.Type.http -> Proxy.Type.HTTP
                    SystemOptions.Proxy.Type.socks4, SystemOptions.Proxy.Type.socks5 -> Proxy.Type.SOCKS
                    else -> Proxy.Type.DIRECT
                }, InetSocketAddress(systemOptions.proxy.host, systemOptions.proxy.port.toInt())
            )

            if (systemOptions.proxy.scope.other) {
                logger.fine("Global other proxy enabled.");

                if (systemOptions.proxy.type == SystemOptions.Proxy.Type.http) {
                    // HTTP/HTTPS Proxy
                    System.setProperty("http.proxyHost", systemOptions.proxy.host)
                    System.setProperty("http.proxyPort", systemOptions.proxy.port)
                    System.setProperty("https.proxyHost", systemOptions.proxy.host)
                    System.setProperty("https.proxyPort", systemOptions.proxy.port)

                    if (systemOptions.proxy.auth) {
                        //val encoded =
                        //    String(Base64.getEncoder().encode("$systemOptions.proxy.username:$systemOptions.proxy.password".toByteArray()))
                        //con.setRequestProperty("Proxy-Authorization", "Basic $encoded")

                        Authenticator.setDefault(ProxyAuth(systemOptions.proxy.username, systemOptions.proxy.password))
                    }
                } else {
                    // SOCKS Proxy
                    System.setProperty("socksProxyHost", systemOptions.proxy.host)
                    System.setProperty("socksProxyPort", systemOptions.proxy.port)

                    if (systemOptions.proxy.auth) {
                        System.setProperty("java.net.socks.username", systemOptions.proxy.username)
                        System.setProperty("java.net.socks.password", systemOptions.proxy.password)

                        Authenticator.setDefault(ProxyAuth(systemOptions.proxy.username, systemOptions.proxy.password))
                    }
                }
            }
        } else {
            proxy = Proxy.NO_PROXY
        }

        val okHttpClientBuilder = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).followRedirects(true)
        if (systemOptions.proxy.scope.driver || systemOptions.proxy.scope.other) {
            okHttpClientBuilder.proxy(proxy).cache(Cache(cacheDirectory, systemOptions.system.cacheSize))
        }
        okHttpClient = okHttpClientBuilder.build()
        //LogSettings.setAsDefaultLogger()
        LogSettings.replaceLogger(okhttp3.internal.platform.Platform::class.java, "logger", "OKHttpClient")
    }

    internal fun setupHttpServer() {
        if (systemOptions.http != null && systemOptions.http.enable) {
            httpServer = Javalin.create {
                it.showJavalinBanner = false
                it.logIfServerNotStarted = true

                if (!systemOptions.http.corsOrigin.isNullOrBlank())
                    it.enableCorsForOrigin(systemOptions.http.corsOrigin)
            }.start(systemOptions.http.host, systemOptions.http.port)

            val gson = GsonBuilder().create()

            JavalinJson.fromJsonMapper = object : FromJsonMapper {
                override fun <T> map(json: String, targetClass: Class<T>) = gson.fromJson(json, targetClass)
            }

            JavalinJson.toJsonMapper = object : ToJsonMapper {
                override fun map(obj: Any): String = gson.toJson(obj)
            }

            httpServer.exception(HttpResponseException::class.java) { e, ctx ->
                ctx.status(e.status)
                ctx.json(APIResult(e.status, e.localizedMessage, APIException(e)))
            }

            httpServer.exception(Exception::class.java) { e, ctx ->
                logger.severe(e, "Unexpected web api exception")
                ctx.status(500)
                ctx.json(APIResult(500, e.localizedMessage, APIException(e)))
            }
        } else {
            logger.info("HTTP API Server not enabled. Some functions may not available.")
        }
    }

    internal fun setupDriver() {
        if (!this::driverThread.isInitialized) {
            driverThread = Thread({
                logger.finer("Starting driver ${this::activeDriver.name}")

                synchronized(loadLock) {
                    activeDriver.onEnable()
                    loadLock.wait()

                    if (!activeDriver.isInitialized) {
                        logger.severe("Bot provider didn't call onBotProviderConnect")
                        exitProcess(19)
                    }
                }
            }, "Driver")

            driverThread.start()
        }
    }

    internal fun setupBotProvider() {
        if (!this::botProviderThread.isInitialized) {
            botProviderThread = Thread({
                try {
                    logger.finer("Starting bot provider ${this::activeBotProvider.name}")

                    synchronized(loadLock) {
                        activeBotProvider.onLoad()
                        activeBotProvider.onEnable()
                        loadLock.notifyAll()
                    }
                } catch (e: Throwable) {
                    Bootstrapper.showErrorAndExit(e, 8, "Bot Provider Initialize Failed")
                }
            }, "Bot Provider")

            botProviderThread.start()
        }
    }

    internal fun beginReadSystemConsole() {
        while (!Thread.interrupted()) {
            val input = readLine()

            if (input?.isNotBlank() == true) {
                consoleInputHandlers.forEach { it.onSystemConsoleInput(input) }
            }
        }
    }

    fun registerSystemConsoleInputHandler(handler: ConsoleReadSupported) {
        consoleInputHandlers.add(handler)
    }

    fun unregisterSystemConsoleInputHandler(handler: ConsoleReadSupported) {
        consoleInputHandlers.remove(handler)
    }

    private class ProxyAuth(user: String, password: String?) : Authenticator() {
        private val auth: PasswordAuthentication =
            PasswordAuthentication(user, password?.toCharArray() ?: charArrayOf())

        override fun getPasswordAuthentication(): PasswordAuthentication {
            return auth
        }
    }

    @FunctionalInterface
    interface ConsoleReadSupported {
        /**
         * On system console input
         * @param input command
         * @return Should stop deliver command to next handler
         */
        fun onSystemConsoleInput(input: String): Boolean
    }
}