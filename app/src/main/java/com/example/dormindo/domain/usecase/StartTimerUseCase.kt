package com.example.dormindo.domain.usecase

import com.example.dormindo.domain.entity.Timer
import com.example.dormindo.domain.repository.TimerRepository
import com.example.dormindo.domain.repository.MediaRepository
import java.time.Duration

/**
 * Use Case para iniciar o timer
 */
class StartTimerUseCase(
    private val timerRepository: TimerRepository,
    private val mediaRepository: MediaRepository
) {
    /**
     * Executa o use case
     * @param duration duração do timer
     * @return Result com o timer criado ou erro
     */
    suspend operator fun invoke(duration: Duration): Result<Timer> {
        return try {
            // Verifica se há mídia sendo reproduzida
            if (!mediaRepository.isMediaPlaying()) {
                return Result.failure(Exception("Nenhuma mídia está sendo reproduzida"))
            }
            
            // Cria o timer
            val timer = Timer.create(duration)
            
            // Inicia o timer
            timerRepository.startTimer(timer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 