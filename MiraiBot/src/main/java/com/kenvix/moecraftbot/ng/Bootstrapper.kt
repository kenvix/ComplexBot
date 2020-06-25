@file:Suppress("ConstantConditionIf")

package com.kenvix.moecraftbot.ng

import com.kenvix.moecraftbot.ng.lib.Sideloader
import com.kenvix.moecraftbot.ng.lib.exception.InvalidConfigException
import com.kenvix.moecraftbot.ng.lib.getStringStackTrace
import org.apache.commons.cli.*
import java.io.File
import com.kenvix.utils.log.Logging
import java.io.IOException
import java.util.logging.Level
import javax.swing.JOptionPane
import kotlin.system.exitProcess

object Bootstrapper : Logging {
    private const val CLI_HEADER = "MoeNetBot Ver.1.0 By Kenvix"

    var isWindowMode = false
        private set

    var isStartedBySideload = false
        private set

    var sideloader: Sideloader<*>? = null
        private set

    override fun getLogTag(): String = "Bootstrapper"

    @JvmStatic
    fun startApplicationBySideload(sideloader: Sideloader<*>, args: Array<String>? = null) {
        isStartedBySideload = true
        logger.info("Starting application from driver")
        this.sideloader = sideloader

        Thread({
            try {
                startApplication(args)
                sideloader.onFinished()

                Defines.beginReadSystemConsole()
            } catch (e: Throwable) {
                showErrorAndExit(e)
            }
        }, "Main").start()
    }


    @JvmStatic
    fun startApplicationBySideload(sideloader: Sideloader<*>, args: String)
        = startApplicationBySideload(sideloader, args.split(" ").toTypedArray())

    @JvmStatic
    fun startApplication(args: Array<String>? = null) {
        val cmd = getCmd(loadExtraArgs(args))

        println(CLI_HEADER)

        try {
            Defines.setupSystem(cmd)
            Defines.setupDatabase()
            Defines.setupNetwork()
            Defines.setupHttpServer()

            Defines.setupDriver()
        } catch (e: InvalidConfigException) {
            showErrorAndExit("There are some errors in your bot config file. Please correct them", 3, e, true)
        } catch (exception: IOException) {
            showErrorAndExit(exception, 2, "This is a Network or IO Exception. May be you need to setup proxy")
        } catch (exception: Exception) {
            showErrorAndExit(exception)
        }
    }

    @JvmOverloads
    fun showErrorAndExit(throwable: Throwable, exitCode: Int = 1, extraMessage: String? = null) {
        showErrorAndExit(
            message = "${if (extraMessage != null) extraMessage + "\n\n" else ""}Message: ${throwable.localizedMessage}\nType:${throwable.javaClass.name}\n\n${throwable.stackTrace.getStringStackTrace()}",
            exitCode = exitCode,
            throwable = throwable
        )
    }

    @JvmOverloads
    fun showErrorAndExit(message: String, exitCode: Int = 1, throwable: Throwable? = null, simpleMessage: Boolean = false) {
        val title = "Application Critical Error! Code #$exitCode"
        logger.error(title)

        when {
            throwable == null -> {
                logger.error(message)
            }
            simpleMessage -> {
                logger.error(message)
                logger.error(throwable.localizedMessage)
            }
            else -> {
                logger.error(message, throwable)
            }
        }

        if (isWindowMode)
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)

        exitProcess(exitCode)
    }

    private fun loadExtraArgs(argsFromOs: Array<String>? = null): Array<String> {
        val optionsFile = File("app.options")

        if (optionsFile.isAbsolute) {
            return if (!optionsFile.exists()) {
                optionsFile.createNewFile()
                logger.info("Created Application argument file in ${optionsFile.absolutePath}")
                argsFromOs ?: arrayOf()
            } else {
                val options = optionsFile.readText()
                logger.info("Found Application argument file in ${optionsFile.absolutePath}")
                logger.info("Loaded arguments from options file: $options")

                if (argsFromOs == null)
                    options.split(" ").toTypedArray()
                else
                    options.split(" ").plus(argsFromOs).toTypedArray()
            }
        }

        return argsFromOs ?: arrayOf()
    }

    @Throws(ParseException::class)
    private fun getCmd(args: Array<String>): CommandLine {
        val ops = Options()

        var driverNames = ""
        for ((key) in Defines.predefinedDriverNameMap) driverNames += " $key"

        ops.addOption("c", "config", true, "Config and data directory path")
        ops.addOption("d", "driver", true, "Driver implemented AbstractDriver (Full Class name or predefined name). Predefined names:$driverNames")
        ops.addOption("v", "verbose", false, "Verbose logging mode.")
        ops.addOption("vv", "very-verbose", false, "Very Verbose logging mode.")
        ops.addOption("vvv", "very-very-verbose", false, "Very Very Verbose logging mode.")
        ops.addOption("h", "help", false, "Print help messages")
        ops.addOption(null, "nogui", false, "No GUI")

        val parser = DefaultParser()
        val cmd = parser.parse(ops, args)

        if (cmd.hasOption("nogui"))
            System.getProperties().setProperty("nogui", "1")

        //TODO: Log Level
        if (cmd.hasOption('v')) {

        } else if (cmd.hasOption("vv")) {

            logger.info("Very Verbose logging mode enabled.")
        } else if (cmd.hasOption("vvv")) {

            logger.info("Very Very Verbose logging mode enabled.")
        } else {

        }

        //LogSettings.setAsDefaultLogger()

        if (cmd.hasOption('h')) {
            logger.trace("Fall into help mode")

            val formatter = HelpFormatter()
            formatter.printHelp("MoeNetBotApp", CLI_HEADER, ops, "", true)
            exitProcess(0)
        }

        logger.trace("Running at JDK ${System.getProperty("java.version")}")
        logger.trace("Scanned integrated Driver. Available:$driverNames")

        return cmd
    }
}