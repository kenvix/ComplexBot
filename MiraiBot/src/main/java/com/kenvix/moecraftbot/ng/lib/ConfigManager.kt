//--------------------------------------------------
// Class ConfigManager
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib

import com.kenvix.moecraftbot.ng.Defines
import com.kenvix.utils.log.Logging
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class ConfigManager<T : Any>
private constructor(private val configFileName: String, val typeClass: Class<in T> = Any::class.java) : Logging {

    private val configFilePathName: String = getConfigFilePathName(configFileName)
    private lateinit var configFileStream: FileInputStream

    lateinit var configYaml: Yaml
        private set

    lateinit var content: T
        private set

    @Suppress("UNCHECKED_CAST")
    private fun load(): Any {
        if (!this::configFileStream.isInitialized) {
            val configFile = File(configFilePathName)

            if (!configFile.exists()) {
                logger.info("Create new config file $configFileName")

                val defaultConfigFileStream = this.javaClass.classLoader.getResourceAsStream("config/$configFileName.yml")
                    ?: throw FileNotFoundException("No default config file $configFileName found")

                defaultConfigFileStream.copyTo(configFile.outputStream())
            }

            configFileStream = FileInputStream(configFile)
            configYaml = Yaml(Constructor(typeClass))
            content = configYaml.load(configFileStream) as T

            logger.info("Loaded config: $configFileName")
        }

        return content
    }

    override fun getLogTag(): String = "ConfigManager"

    companion object {
        @JvmStatic
        fun getConfigFilePathName(configFileName: String) = "${Defines.baseConfigPath}config/$configFileName.yml"

        @JvmStatic
        val configMap = hashMapOf<String, ConfigManager<*>>()

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> getConfigManager(configName: String, typeClass: Class<in T>): ConfigManager<T> {
            if (!configMap.containsKey(configName)) {
                val configInstance = ConfigManager(configName, typeClass)
                configInstance.load()

                configMap[configName] = configInstance

                return configInstance
            }

            return configMap[configName]!! as ConfigManager<T>
        }



        fun getConfigManager(configName: String): ConfigManager<Any> {
            return getConfigManager(configName, Any::class.java)
        }
    }
}