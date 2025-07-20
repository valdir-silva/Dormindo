package com.example.dormindo.domain.usecase

import com.example.dormindo.domain.repository.TimerRepository
import com.example.dormindo.domain.repository.MediaRepository

/**
 * Use Case para parar o timer
 */
class StopTimerUseCase(
    private val timerRepository: TimerRepository,
    private val mediaRepository: MediaRepository
) {
    /**
     * Executa o use case
     * @return Result com o resultado da operação
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Para o timer
            val timerResult = timerRepository.stopTimer()
            
            if (timerResult.isSuccess) {
                // Para a reprodução de mídia
                val mediaResult = mediaRepository.stopMediaPlayback()
                if (mediaResult.isFailure) {
                    return mediaResult
                }
                return timerResult
            }
            timerResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 