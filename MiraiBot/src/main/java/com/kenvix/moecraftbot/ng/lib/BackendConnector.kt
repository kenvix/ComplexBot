package com.kenvix.moecraftbot.ng.lib

import com.kenvix.android.utils.Coroutines
import org.apache.thrift.TServiceClient
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.protocol.TProtocol
import org.newsclub.net.unix.AFUNIXSocket
import org.newsclub.net.unix.AFUNIXSocketAddress
import org.slf4j.Logger
import com.kenvix.utils.exception.NotSupportedException
import kotlinx.coroutines.*
import org.apache.thrift.transport.*
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.channels.Channel
import java.nio.channels.SocketChannel

@Suppress("MemberVisibilityCanBePrivate", "unused")

/**
 * Backend IPC Connector
 *
 * @param backendHost Host address of backend. If starts with unix:, it will be considered as UNIX Socket path
 * @param backendPort Port of backend. If backendHost is unix socket path, it will be ignored
 */
class BackendConnector <T: TServiceClient> (
        val clientClass: Class<T>,
        val backendHost: String,
        val backendPort: Int,
        val connectTimeout: Int = 5000,
        val readTimeout: Int = 0
) : Closeable {

    lateinit var client: T
        private set

    private lateinit var transportSocket: TTransport
    private lateinit var socket: TTransport
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val coroutine = Coroutines()

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun connect(keepSocketAlive: Boolean = true) = withContext(Dispatchers.IO) {
        logger.info("Connecting to backend on $backendHost:$backendPort")

        socket = if (backendHost.startsWith("unix:")) {
            // Unstable, test only.
            logger.warn("Unix socket backend is used for test only.")

            TSocket(AFUNIXSocket.newInstance().apply {
                soTimeout = readTimeout
                connect(AFUNIXSocketAddress(File(backendHost.substring("unix:".length))), connectTimeout)
            })
        } else {
//            Socket(backendHost, backendPort).apply {
//                soTimeout = readTimeout
//                connect(InetSocketAddress(backendHost, backendPort), connectTimeout)
//            }
            TSocket(backendHost, backendPort, connectTimeout).apply {
                open()
            }
//            TNonblockingSocket(backendHost, backendPort, connectTimeout).also {
//                if (!it.socketChannel.isConnected) {
//                    it.socketChannel.connect(InetSocketAddress(backendHost, backendPort))
//                    it.socketChannel.finishConnect()
//                }
//            }
        }

        transportSocket = socket.let {
            when (it) {
                is TSocket -> it
                is Socket -> TSocket(it)
                is SocketChannel -> TNonblockingSocket(it)
                is TNonblockingSocket -> it
                else -> throw IllegalArgumentException("Unknown socket type")
            }
        }


        val transport = TFramedTransport(transportSocket)
        val protocol = TBinaryProtocol(transport)
        client = clientClass.getConstructor(TProtocol::class.java).newInstance(protocol)

//        transportSocket.setTimeout(connectTimeout)

        try {
            ping()
        } catch (e: Exception) {
            if (e is NoSuchMethodException || e is IllegalAccessException)
                logger.debug("Backend has no ping(String) method or inaccessible, ignored connection test.")
            else
                throw e
        }

        if (keepSocketAlive && socket is Socket)
            keepSocketAlive()

        client
    }

    private fun keepSocketAlive() {
        coroutine.ioScope.launch {
            while (isActive) {
                if (transportSocket.isOpen) {
                    delay(300)
                } else {
                    try {
                        logger.warn("Connection to backend lost, reconnecting ...")
                        connect(true)
                        delay(100)

                        break
                    } catch (e: Exception) {
                        logger.error("Reconnection failed, retrying after 3s ...", e)
                        delay(3000)
                    }
                }
            }
        }
    }

    override fun close() {
        GlobalScope.launch { closeAndWait() }
    }

    @Throws(NoSuchMethodException::class, NotSupportedException::class)
    fun ping() {
        val pingMethod = clientClass.getMethod("ping", String::class.java)

        try {
            if (pingMethod.invoke(client, "hello") == "hello") {
                logger.info("Backend connected")
            } else {
                throw NotSupportedException("Not supported client: Unrecognized backend reply. expected hello")
            }
        } catch (e: InvocationTargetException) {
            if (e.targetException != null)
                throw e.targetException
            else
                throw e
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun closeAndWait() = withContext(Dispatchers.IO) {
        coroutine.close()
        socket.close()
    }
}