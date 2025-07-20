package com.example.dormindo.domain.usecase

import com.example.dormindo.domain.repository.TimerRepository
import com.example.dormindo.domain.repository.MediaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StopTimerUseCaseTest {
    private lateinit var timerRepository: TimerRepository
    private lateinit var mediaRepository: MediaRepository
    private lateinit var useCase: StopTimerUseCase

    @Before
    fun setUp() {
        timerRepository = mockk(relaxed = true)
        mediaRepository = mockk(relaxed = true)
        useCase = StopTimerUseCase(timerRepository, mediaRepository)
    }

    @Test
    fun `deve parar timer e mídia quando timer for parado com sucesso`() = runBlocking {
        // Arrange
        coEvery { timerRepository.stopTimer() } returns Result.success(Unit)
        coEvery { mediaRepository.stopMediaPlayback() } returns Result.success(Unit)

        // Act
        val result = useCase.invoke()

        // Assert
        assertTrue(result.isSuccess)
        coVerify { timerRepository.stopTimer() }
        coVerify { mediaRepository.stopMediaPlayback() }
    }

    @Test
    fun `deve retornar falha quando timer falhar ao parar`() = runBlocking {
        // Arrange
        coEvery { timerRepository.stopTimer() } returns Result.failure(Exception("Erro ao parar timer"))

        // Act
        val result = useCase.invoke()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Erro ao parar timer", result.exceptionOrNull()?.message)
        coVerify { timerRepository.stopTimer() }
        coVerify(exactly = 0) { mediaRepository.stopMediaPlayback() }
    }

    @Test
    fun `deve retornar falha quando mídia falhar ao parar`() = runBlocking {
        // Arrange
        coEvery { timerRepository.stopTimer() } returns Result.success(Unit)
        coEvery { mediaRepository.stopMediaPlayback() } returns Result.failure(Exception("Erro ao parar mídia"))

        // Act
        val result = useCase.invoke()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Erro ao parar mídia", result.exceptionOrNull()?.message)
        coVerify { timerRepository.stopTimer() }
        coVerify { mediaRepository.stopMediaPlayback() }
    }
} 