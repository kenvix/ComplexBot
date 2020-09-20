//--------------------------------------------------
// Class ApplicationPhase
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib

enum class ApplicationPhase(val code: Byte) {
    NotInitialized(0),
    Initializing(1),
    Running(2),
    Stopping(3),
    Stopped(4)
}