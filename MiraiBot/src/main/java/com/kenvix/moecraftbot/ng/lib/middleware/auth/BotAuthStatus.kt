package com.kenvix.moecraftbot.ng.lib.middleware.auth

enum class BotAuthStatus {
    Idle,
    WaitingUsername,
    WaitingPassword,
    Authenticating,
    Done
}