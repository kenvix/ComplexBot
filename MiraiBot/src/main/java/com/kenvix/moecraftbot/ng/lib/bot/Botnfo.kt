//--------------------------------------------------
// Class BotFrameworkInfo
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.bot

import com.kenvix.moecraftbot.ng.BuildConfig

class BotFrameworkInfo internal constructor() {
        val version = BuildConfig.VERSION
        val name = BuildConfig.NAME
        val applicationName = BuildConfig.APPLICATION_NAME
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        val builtAt = BuildConfig.BUILD_UNIXTIME
        val isRelease = BuildConfig.IS_RELEASE_BUILD
}