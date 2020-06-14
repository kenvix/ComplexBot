//--------------------------------------------------
// Interface ManagaedConfig
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib

import com.kenvix.utils.log.Logging
import java.lang.reflect.ParameterizedType

abstract class BaseFunctionalEntity<T: Any> : Logging {
    abstract val configFileName: String
    lateinit var configTypeClass: Class<in T>
        private set

    lateinit var config: ConfigManager<T>
        private set

    lateinit var options: T
        private set

    fun loadConfig() {
        val typeArguments = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments

        @Suppress("UNCHECKED_CAST")
        configTypeClass = typeArguments[0] as Class<in T>

        config = ConfigManager.getConfigManager(configFileName, configTypeClass)
        options = config.content
    }
}