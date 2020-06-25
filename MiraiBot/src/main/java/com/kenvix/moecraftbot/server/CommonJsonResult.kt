//--------------------------------------------------
// Class CommonJsonResult
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.server

data class CommonJsonResult(
        val status: Int,
        val code: Int = status,
        val info: String = "",
        val data: Any? = null
)