package net.alunando.dormindo.domain.usecase

import net.alunando.dormindo.domain.entity.TimerStatus
import net.alunando.dormindo.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case para obter o status do timer
 */
class GetTimerStatusUseCase(
    private val timerRepository: TimerRepository
) {
    /**
     * Executa o use case
     * @return Flow com o status atual do timer
     */
    operator fun invoke(): Flow<TimerStatus> {
        return timerRepository.getTimerStatus()
    }
} 