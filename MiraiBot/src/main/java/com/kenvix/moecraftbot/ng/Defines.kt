package com.kenvix.moecraftbot.ng

import com.kenvix.moecraftbot.mirai.lib.bot.AbstractDriver
import com.kenvix.moecraftbot.ng.lib.*
import com.kenvix.moecraftbot.ng.lib.exception.InvalidConfigException
import com.kenvix.utils.event.EventSet
import com.kenvix.utils.event.eventSetOf
import com.kenvix.utils.log.Logging
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.apache.commons.cli.CommandLine
import org.apache.commons.dbcp2.BasicDataSource
import org.bson.BsonDocument
import org.bson.BsonString
import org.bson.conversions.Bson
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.io.File
import java.io.FileNotFoundException
import java.net.*
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
    lateinit var appCmds: CommandLine
        private set

    @JvmStatic
    val predefinedDriverNameMap = mapOf<String, String>(
            "demobot" to "com.kenvix.moecraftbot.ng.driver.demobot.DemoBotDriver",
            "complexbot" to "com.kenvix.complexbot.ComplexBotDriver"
    )

    lateinit var driverThread: Thread
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

    lateinit var mongoClient: CoroutineClient
        private set

    lateinit var mongoDatabase: CoroutineDatabase
        private set

    val shutdownHandler: EventSet<Unit> = eventSetOf()

    @JvmStatic
    lateinit var cachedThreadPool: ExecutorService
        private set

    private val loadLock = java.lang.Object()
    private val consoleInputHandlers: MutableList<ConsoleReadSupported> = LinkedList()

    val startedAt = System.currentTimeMillis()

    lateinit var pluginClassLoader: URLClassLoader
        private set

    internal fun setupSystem(appCmds: CommandLine = CommandLine.Builder().build()) {
        logger.info("Loading Application")
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"))
        CachedClasses
        ExceptionHandler.registerGlobalExceptionHandler()

        this.appCmds = appCmds
        baseConfigPath = appCmds.getOptionValue('c', "config")

        if (!baseConfigPath.endsWith('/'))
            baseConfigPath += "/"

        run {
            val configDirFile = Paths.get(baseConfigPath).resolve("config").toFile()
            if (!configDirFile.exists())
                configDirFile.mkdirs()
        }

        Runtime.getRuntime().addShutdownHook(Thread({ shutdownSystem() }, "Shutdown Callback"))

        //Load system (2)
        systemOptions = ConfigManager.getConfigManager("system", SystemOptions::class.java).content
        cachedThreadPool = ThreadPoolExecutor(1, systemOptions.system.threadPoolMaxSize,
            60L, TimeUnit.SECONDS, SynchronousQueue<Runnable>())
        cacheDirectory = File("cache")
    }

    internal fun setupPlugins() {
        val pluginDir = File("plugins")
        if (!pluginDir.exists())
            pluginDir.mkdirs()

        val pluginLibDir = pluginDir.toPath().resolve("libs").toFile()
        if (!pluginLibDir.exists())
            pluginLibDir.mkdirs()

        logger.info("Loading plugins from ${pluginDir.absolutePath}")
        pluginClassLoader = URLClassLoader(arrayOf(pluginDir.toURI().toURL(), pluginLibDir.toURI().toURL()))
    }

    internal fun setupDriverPre() {
        //Load bot driver
        val driverName = appCmds.getOptionValue('d') ?: "demobot"

        val driverClass = if (predefinedDriverNameMap.containsKey(driverName)) {
            Class.forName(predefinedDriverNameMap[driverName.toLowerCase()], true, pluginClassLoader)
        } else {
            Class.forName(driverName, true, pluginClassLoader)
        }

        activeDriver = driverClass.getConstructor().newInstance() as AbstractDriver<*>
        consoleInputHandlers.add(activeDriver)
    }

    internal fun setupSQLDatabase() {
        System.getProperties().setProperty("org.jooq.no-logo", "true")
        dataSource = BasicDataSource()

        when(systemOptions.database.type.toLowerCase()) {
            "sqlite" -> {
                val databaseFileName = if (systemOptions.database.host.isNullOrBlank()) "database.sqlite3" else systemOptions.database.host
                val databaseFile = Paths.get(baseConfigPath).resolve(databaseFileName).toFile()
                val jdbcUriString = "jdbc:sqlite:${databaseFile.canonicalPath}"

                if (!databaseFile.exists()) {
                    logger.info("Create new database file $databaseFileName")

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
                logger.warn("Warning: No database enabled. If any modules using database was loaded, unexpected errors may occurs.")
            }

            else -> throw InvalidConfigException("Invalid database type ${systemOptions.database.type}")
        }

        dataSource.initialSize = 1
        dataSource.minIdle = 1
        dataSource.maxIdle = 3

        logger.info("Database ready: ${systemOptions.database.type}://${systemOptions.database.host}")
    }

    internal fun setupMongoDatabase() = runBlocking {
        val address = ServerAddress(systemOptions.mongo.host, systemOptions.mongo.port)
        val conn = ConnectionString("mongodb://${address.host}:" +
                "${address.port}/${systemOptions.mongo.name}?w=majority")

        logger.info("Loading Mongo Database: $conn")


        val options = MongoClientSettings.builder().apply {
            applicationName("MoeCraftBot/Mirai")

            if (systemOptions.mongo.needAuth) {
                val credential = MongoCredential.createCredential(systemOptions.mongo.user,
                        systemOptions.mongo.authSource, systemOptions.mongo.password.toCharArray())
                credential(credential)
            }

            applyConnectionString(conn)
        }.build()

        mongoClient = KMongo.createClient(options).coroutine
        mongoDatabase = mongoClient.getDatabase(systemOptions.mongo.name)

        logger.info("Mongo Database connection test pass!" +
                " ${mongoDatabase.listCollectionNames().size} collections present")
    }

    internal fun setupNetwork() {
        if (systemOptions.proxy.enable) {
            logger.info("Proxy enabled: ${systemOptions.proxy.type}://${systemOptions.proxy.host}:${systemOptions.proxy.port}")

            proxy = Proxy(
                when (systemOptions.proxy.type) {
                    SystemOptions.Proxy.Type.http -> Proxy.Type.HTTP
                    SystemOptions.Proxy.Type.socks4, SystemOptions.Proxy.Type.socks5 -> Proxy.Type.SOCKS
                    else -> Proxy.Type.DIRECT
                }, InetSocketAddress(systemOptions.proxy.host, systemOptions.proxy.port.toInt())
            )

            if (systemOptions.proxy.scope.other) {
                logger.info("Global other proxy enabled.");

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
    }

    internal fun setupHttpServer() {

    }

    internal fun setupDriver() {
        if (!this::driverThread.isInitialized) {
            driverThread = Thread({
                logger.info("Starting driver ${this::activeDriver.name}")

                synchronized(loadLock) {
                    try {
                        activeDriver.onEnable()
                        loadLock.wait()

                        if (!activeDriver.isInitialized) {
                            logger.error("Bot provider didn't call onBotProviderConnect")
                            exitProcess(19)
                        }
                    } catch (e: InterruptedException) {
                        logger.trace("setupDriver Interrupted. Application maybe going to shutdown")
                    }
                }
            }, "Driver")

            driverThread.start()
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

    private fun shutdownSystem() {
        logger.info("Shutdown system ...")
        synchronized(loadLock) {
            if (this::activeDriver.isInitialized)
                activeDriver.onDisable()

            shutdownHandler(Unit)

            if (this::driverThread.isInitialized)
                driverThread.interrupt()

            if (this::dslContext.isInitialized)
                dslContext.close()

            if (this::dataSource.isInitialized)
                dataSource.close()

            if (this::cachedThreadPool.isInitialized)
                cachedThreadPool.shutdown()
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