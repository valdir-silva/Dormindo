package net.alunando.dormindo.domain.entity

import java.time.Duration
import java.util.UUID

/**
 * Entidade que representa um timer para parar reprodução de mídia
 */
data class Timer(
    val id: String = "",
    val duration: Duration,
    val isActive: Boolean = false,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val remainingTime: Duration = Duration.ofSeconds(0)
) {
    companion object {
        fun create(duration: Duration): Timer {
            return Timer(
                id = "${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}",
                duration = duration
            )
        }
    }
} 