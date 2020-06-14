//--------------------------------------------------
// Class APIResult
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.api

data class APIResult<T : Any?>(
    val code: Int,
    val info: String = "",
    val data: T? = null
)