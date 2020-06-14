//--------------------------------------------------
// Class Sideloader
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib

interface Sideloader<T> {
    fun getObject(): T
    fun onFinished()
}