package net.alunando.dormindo

import net.alunando.dormindo.domain.entity.Timer
import net.alunando.dormindo.domain.usecase.StartTimerUseCase
import net.alunando.dormindo.domain.repository.TimerRepository
import net.alunando.dormindo.domain.repository.MediaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class StartTimerUseCaseTest {
    private lateinit var timerRepository: TimerRepository
    private lateinit var mediaRepository: MediaRepository
    private lateinit var useCase: StartTimerUseCase

    @Before
    fun setUp() {
        timerRepository = mockk(relaxed = true)
        mediaRepository = mockk(relaxed = true)
        useCase = StartTimerUseCase(timerRepository, mediaRepository, "No media playing")
    }

    @Test
    fun `deve iniciar timer quando houver mídia tocando`() = runBlocking {
        // Arrange
        val duration = Duration.ofMinutes(30)
        val timer = Timer.create(duration)
        coEvery { mediaRepository.isMediaPlaying() } returns true
        coEvery { timerRepository.startTimer(any()) } returns Result.success(timer)

        // Act
        val result = useCase.invoke(duration)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(timer, result.getOrNull())
        coVerify { timerRepository.startTimer(any()) }
    }

    @Test
    fun `nao deve iniciar timer se nenhuma mídia estiver tocando`() = runBlocking {
        // Arrange
        val duration = Duration.ofMinutes(30)
        coEvery { mediaRepository.isMediaPlaying() } returns false

        // Act
        val result = useCase.invoke(duration)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("No media playing", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { timerRepository.startTimer(any()) }
    }

    @Test
    fun `deve retornar falha se repository falhar`() = runBlocking {
        // Arrange
        val duration = Duration.ofMinutes(30)
        coEvery { mediaRepository.isMediaPlaying() } returns true
        coEvery { timerRepository.startTimer(any()) } returns Result.failure(Exception("Erro ao iniciar timer"))

        // Act
        val result = useCase.invoke(duration)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Erro ao iniciar timer", result.exceptionOrNull()?.message)
    }
} 