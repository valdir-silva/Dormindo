package net.alunando.dormindo.domain.usecase

import net.alunando.dormindo.domain.entity.TimerStatus
import net.alunando.dormindo.domain.repository.TimerRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class GetTimerStatusUseCaseTest {
    private lateinit var timerRepository: TimerRepository
    private lateinit var useCase: GetTimerStatusUseCase

    @Before
    fun setUp() {
        timerRepository = mockk(relaxed = true)
        useCase = GetTimerStatusUseCase(timerRepository)
    }

    @Test
    fun `deve retornar status idle quando timer estiver inativo`() = runBlocking {
        // Arrange
        val expectedStatus = TimerStatus.Idle
        every { timerRepository.getTimerStatus() } returns flowOf(expectedStatus)

        // Act
        val result = useCase.invoke().first()

        // Assert
        assertEquals(expectedStatus, result)
        verify { timerRepository.getTimerStatus() }
    }

    @Test
    fun `deve retornar status running quando timer estiver ativo`() = runBlocking {
        // Arrange
        val expectedStatus = TimerStatus.Running
        every { timerRepository.getTimerStatus() } returns flowOf(expectedStatus)

        // Act
        val result = useCase.invoke().first()

        // Assert
        assertEquals(expectedStatus, result)
        verify { timerRepository.getTimerStatus() }
    }

    @Test
    fun `deve retornar status paused com tempo restante`() = runBlocking {
        // Arrange
        val remainingTime = Duration.ofMinutes(15)
        val expectedStatus = TimerStatus.Paused(remainingTime)
        every { timerRepository.getTimerStatus() } returns flowOf(expectedStatus)

        // Act
        val result = useCase.invoke().first()

        // Assert
        assertEquals(expectedStatus, result)
        assertTrue(result is TimerStatus.Paused)
        assertEquals(remainingTime, (result as TimerStatus.Paused).remainingTime)
        verify { timerRepository.getTimerStatus() }
    }

    @Test
    fun `deve retornar status completed quando timer terminar`() = runBlocking {
        // Arrange
        val expectedStatus = TimerStatus.Completed
        every { timerRepository.getTimerStatus() } returns flowOf(expectedStatus)

        // Act
        val result = useCase.invoke().first()

        // Assert
        assertEquals(expectedStatus, result)
        verify { timerRepository.getTimerStatus() }
    }

    @Test
    fun `deve retornar status error com mensagem`() = runBlocking {
        // Arrange
        val errorMessage = "Erro no timer"
        val expectedStatus = TimerStatus.Error(errorMessage)
        every { timerRepository.getTimerStatus() } returns flowOf(expectedStatus)

        // Act
        val result = useCase.invoke().first()

        // Assert
        assertEquals(expectedStatus, result)
        assertTrue(result is TimerStatus.Error)
        assertEquals(errorMessage, (result as TimerStatus.Error).message)
        verify { timerRepository.getTimerStatus() }
    }
} 