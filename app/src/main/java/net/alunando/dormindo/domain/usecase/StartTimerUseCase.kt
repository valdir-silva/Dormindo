package net.alunando.dormindo.domain.usecase

import net.alunando.dormindo.domain.entity.Timer
import net.alunando.dormindo.domain.repository.TimerRepository
import net.alunando.dormindo.domain.repository.MediaRepository
import java.time.Duration

/**
 * Use Case para iniciar o timer
 */
class StartTimerUseCase(
    private val timerRepository: TimerRepository,
    private val mediaRepository: MediaRepository,
    private val noMediaPlayingErrorMessage: String
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
                return Result.failure(Exception(noMediaPlayingErrorMessage))
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