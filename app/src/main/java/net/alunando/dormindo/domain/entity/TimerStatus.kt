package net.alunando.dormindo.domain.entity

import java.time.Duration

/**
 * Status atual do timer
 */
sealed class TimerStatus {
    object Idle : TimerStatus()
    object Running : TimerStatus()
    data class Paused(val remainingTime: Duration) : TimerStatus()
    object Completed : TimerStatus()
    data class Error(val message: String) : TimerStatus()
} 