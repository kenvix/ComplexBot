@file:Suppress("ConstantConditionIf")

package com.kenvix.moecraftbot.ng

import com.kenvix.moecraftbot.ng.lib.Sideloader
import com.kenvix.moecraftbot.ng.lib.exception.InvalidConfigException
import com.kenvix.moecraftbot.ng.lib.format
import com.kenvix.moecraftbot.ng.lib.getStringStackTrace
import com.kenvix.moecraftbot.ng.lib.gui.GeneralGui
import com.kenvix.utils.log.LogSettings
import com.kenvix.utils.log.Logging
import com.kenvix.utils.log.severe
import org.apache.commons.cli.*
import java.io.File
import java.io.IOException
import java.util.logging.Level
import javax.swing.JOptionPane
import kotlin.system.exitProcess

object Bootstrapper : Logging {
    private const val CLI_HEADER = "${BuildConfig.APPLICATION_NAME} Ver.${BuildConfig.VERSION_NAME} By Kenvix"

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
        logger.finest("Starting application from driver")
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
        if (System.getProperties().getProperty("nogui") == null && (BuildConfig.IS_RELEASE_BUILD || isStartedBySideload) && System.console() == null) {
            isWindowMode = true
            showGuiCommandWindow()
        }

        println(CLI_HEADER)

        try {
            Defines.setupSystem(cmd)
            Defines.setupDatabase()
            Defines.setupNetwork()
            Defines.setupHttpServer()

            Defines.setupDriver()
            Defines.setupBotProvider()
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
        logger.severe(title)

        if (throwable == null) {
            logger.severe(message)
        } else if (simpleMessage) {
            logger.severe(message)
            logger.severe(throwable.localizedMessage)
        } else {
            logger.severe(throwable, message)
        }

        if (isWindowMode)
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)

        exitProcess(exitCode)
    }

    private fun showGuiCommandWindow() {
        val gui = GeneralGui()
        System.setErr(gui.printStream)
        System.setOut(gui.printStream)
        gui.show()
    }

    private fun loadExtraArgs(argsFromOs: Array<String>? = null): Array<String> {
        val optionsFile = File("app.options")

        if (optionsFile.isAbsolute) {
            if (!optionsFile.exists()) {
                optionsFile.createNewFile()
                logger.finer("Created Application argument file in ${optionsFile.absolutePath}")
                return argsFromOs ?: arrayOf()
            } else {
                val options = optionsFile.readText()
                logger.finer("Found Application argument file in ${optionsFile.absolutePath}")
                logger.finest("Loaded arguments from options file: $options")

                if (argsFromOs == null)
                    return options.split(" ").toTypedArray()
                else
                    return options.split(" ").plus(argsFromOs).toTypedArray()
            }
        }

        return argsFromOs ?: arrayOf()
    }

    @Throws(ParseException::class)
    private fun getCmd(args: Array<String>): CommandLine {
        val ops = Options()

        var driverNames = ""
        for ((key) in Defines.predefinedDriverNameMap) driverNames += " $key"

        var botProviderNames = ""
        for ((key) in Defines.predefinedBotProviderNameMap) botProviderNames += " $key"

        ops.addOption("c", "config", true, "Config and data directory path")
        ops.addOption("d", "driver", true, "Driver implemented AbstractDriver (Full Class name or predefined name). Predefined names:$driverNames")
        ops.addOption("p", "provider", true, "Bot provider (Full Class name or predefined name). Predefined names:$botProviderNames")
        ops.addOption("v", "verbose", false, "Verbose logging mode.")
        ops.addOption("vv", "very-verbose", false, "Very Verbose logging mode.")
        ops.addOption("vvv", "very-very-verbose", false, "Very Very Verbose logging mode.")
        ops.addOption("h", "help", false, "Print help messages")
        ops.addOption(null, "nogui", false, "No GUI")

        val parser = DefaultParser()
        val cmd = parser.parse(ops, args)

        if (cmd.hasOption("nogui"))
            System.getProperties().setProperty("nogui", "1")

        if (!BuildConfig.IS_RELEASE_BUILD)
            logger.finest("Debug build")

        if (cmd.hasOption('v')) {
            LogSettings.setLevel(Level.FINER)
        } else if (cmd.hasOption("vv")) {
            LogSettings.setLevel(Level.FINEST)
            logger.log(Level.FINER, "Very Verbose logging mode enabled.")
        } else if (cmd.hasOption("vvv")) {
            LogSettings.setLevel(Level.ALL)
            logger.log(Level.FINER, "Very Very Verbose logging mode enabled.")
        } else {
            LogSettings.setLevel(Level.FINE)
        }

        //LogSettings.setAsDefaultLogger()

        if (cmd.hasOption('h')) {
            logger.finest("Fall into help mode")

            val formatter = HelpFormatter()
            formatter.printHelp(BuildConfig.APPLICATION_NAME, CLI_HEADER, ops, "", true)
            exitProcess(0)
        }

        logger.finest("Built at ${BuildConfig.BUILD_DATE.format()} By ${BuildConfig.BUILD_USER} @ ${BuildConfig.BUILD_OS} JDK ${BuildConfig.BUILD_JDK}")
        logger.finest("Running at JDK ${System.getProperty("java.version")}")
        logger.finest("Scanned integrated Bot provider. Available:$botProviderNames")
        logger.finest("Scanned integrated Driver. Available:$driverNames")

        return cmd
    }
}