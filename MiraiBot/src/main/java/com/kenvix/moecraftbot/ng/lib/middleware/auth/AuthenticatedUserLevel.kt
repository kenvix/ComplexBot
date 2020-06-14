//--------------------------------------------------
// Enum AuthedUserLevel
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.middleware.auth

enum class AuthenticatedUserLevel(val levelCode: Byte) {
    PENDING(0),
    BANNED (0b0001),
    USER   (0b0010),
    ADMIN  (0b1000);
}