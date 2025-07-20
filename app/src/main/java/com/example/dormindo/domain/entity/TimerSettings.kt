package com.example.dormindo.domain.entity

import java.time.Duration

/**
 * Configurações do timer
 */
data class TimerSettings(
    val defaultDuration: Duration = Duration.ofMinutes(30),
    val showNotification: Boolean = true,
    val vibrateOnComplete: Boolean = false,
    val autoStart: Boolean = false,
    val theme: Theme = Theme.DARK
) {
    enum class Theme {
        LIGHT, DARK, SYSTEM
    }
} 