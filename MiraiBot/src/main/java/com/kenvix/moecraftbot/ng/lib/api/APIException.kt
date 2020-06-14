//--------------------------------------------------
// Class APIException
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.api

class APIException(e: Exception) {
    val name: String = e.javaClass.simpleName

    override fun toString(): String {
        return "APIException($name)"
    }
}