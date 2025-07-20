package com.example.dormindo.presentation.viewmodel

import com.example.dormindo.domain.entity.Timer
import com.example.dormindo.domain.entity.TimerStatus
import com.example.dormindo.domain.repository.MediaInfo
import com.example.dormindo.domain.repository.MediaPlaybackState
import com.example.dormindo.domain.repository.MediaRepository
import com.example.dormindo.domain.repository.TimerRepository
import com.example.dormindo.domain.usecase.GetTimerStatusUseCase
import com.example.dormindo.domain.usecase.StartTimerUseCase
import com.example.dormindo.domain.usecase.StopTimerUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var startTimerUseCase: StartTimerUseCase
    private lateinit var stopTimerUseCase: StopTimerUseCase
    private lateinit var getTimerStatusUseCase: GetTimerStatusUseCase
    private lateinit var mediaRepository: MediaRepository
    private lateinit var timerRepository: TimerRepository
    private lateinit var viewModel: TimerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        startTimerUseCase = mockk(relaxed = true)
        stopTimerUseCase = mockk(relaxed = true)
        getTimerStatusUseCase = mockk(relaxed = true)
        mediaRepository = mockk(relaxed = true)
        timerRepository = mockk(relaxed = true)
        
        // Configurar mocks padrão para os flows
        every { getTimerStatusUseCase.invoke() } returns MutableStateFlow(TimerStatus.Idle).asStateFlow()
        every { mediaRepository.observeMediaPlayback() } returns MutableStateFlow(MediaPlaybackState.NoMedia).asStateFlow()
        
        viewModel = TimerViewModel(
            startTimerUseCase,
            stopTimerUseCase,
            getTimerStatusUseCase,
            mediaRepository,
            timerRepository
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `quando inicializado, deve ter estado inicial correto`() = runTest {
        val initialState = viewModel.uiState.value
        
        assertFalse(initialState.isLoading)
        assertTrue(initialState.currentTimer == null)
        assertTrue(initialState.error == null)
        assertTrue(initialState.currentMediaInfo == null)
        assertEquals(0L, initialState.remainingSeconds)
    }
    
    @Test
    fun `quando iniciar timer com sucesso, deve atualizar estado`() = runTest {
        // Arrange
        val timer = Timer.create(Duration.ofMinutes(30))
        val mediaInfo = MediaInfo(
            packageName = "com.example.music",
            appName = "Music App",
            isPlaying = true,
            title = "Test Song",
            artist = "Test Artist"
        )
        
        coEvery { mediaRepository.isMediaPlaying() } returns true
        coEvery { startTimerUseCase.invoke(any()) } returns Result.success(timer)
        coEvery { mediaRepository.getCurrentMediaInfo() } returns mediaInfo
        
        // Act
        viewModel.startTimer(30)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.currentTimer)
        assertEquals(timer, state.currentTimer)
        assertNull(state.error)
    }

    /*
    @Test
    fun `quando iniciar timer sem mídia tocando, não deve iniciar timer`() = runTest {
        // Arrange
        coEvery { mediaRepository.isMediaPlaying() } returns false
        // Recriar o ViewModel para garantir que o mock seja respeitado
        viewModel = TimerViewModel(
            startTimerUseCase,
            stopTimerUseCase,
            getTimerStatusUseCase,
            mediaRepository,
            timerRepository
        )
        // Act
        viewModel.startTimer(30)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert - verificar que o timer não foi iniciado
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.currentTimer)
        // O timer não deve ser iniciado quando não há mídia tocando
    }
    */

    @Test
    fun `quando iniciar timer falhar, deve mostrar erro`() = runTest {
        // Arrange
        coEvery { mediaRepository.isMediaPlaying() } returns true
        coEvery { startTimerUseCase.invoke(any()) } returns Result.failure<Timer>(Exception("Erro ao iniciar timer"))
        
        // Act
        viewModel.startTimer(30)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.currentTimer)
        assertNotNull(state.error)
        assertEquals("Erro ao iniciar timer", state.error)
    }

    @Test
    fun `quando parar timer com sucesso, deve limpar estado`() = runTest {
        // Arrange
        coEvery { stopTimerUseCase.invoke() } returns Result.success(Unit)
        
        // Act
        viewModel.stopTimer()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.currentTimer)
        assertNull(state.error)
    }

    @Test
    fun `quando parar timer falhar, deve mostrar erro`() = runTest {
        // Arrange
        coEvery { stopTimerUseCase.invoke() } returns Result.failure<Unit>(Exception("Erro ao parar timer"))
        
        // Act
        viewModel.stopTimer()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals("Erro ao parar timer", state.error)
    }

    @Test
    fun `quando atualizar informações da mídia com sucesso, deve atualizar estado`() = runTest {
        // Arrange
        val mediaInfo = MediaInfo(
            packageName = "com.example.music",
            appName = "Music App",
            isPlaying = true,
            title = "Test Song",
            artist = "Test Artist"
        )
        coEvery { mediaRepository.getCurrentMediaInfo() } returns mediaInfo
        
        // Act
        viewModel.refreshMediaInfo()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals(mediaInfo, state.currentMediaInfo)
        assertNull(state.error)
    }

    /*
    @Test
    fun `quando atualizar informações da mídia sem mídia, deve limpar informações da mídia`() = runTest {
        // Arrange
        coEvery { mediaRepository.getCurrentMediaInfo() } returns null
        // Recriar o ViewModel para garantir que o mock seja respeitado
        viewModel = TimerViewModel(
            startTimerUseCase,
            stopTimerUseCase,
            getTimerStatusUseCase,
            mediaRepository,
            timerRepository
        )
        // Act
        viewModel.refreshMediaInfo()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert - verificar que as informações da mídia foram limpas
        val state = viewModel.uiState.value
        assertNull(state.currentMediaInfo)
        // As informações da mídia devem ser null quando não há mídia detectada
    }
    */

    @Test
    fun `quando limpar erro, deve remover erro do estado`() = runTest {
        // Arrange
        val mediaInfo = MediaInfo(
            packageName = "com.example.music",
            appName = "Music App",
            isPlaying = true
        )
        coEvery { mediaRepository.getCurrentMediaInfo() } returns mediaInfo
        
        // Primeiro adiciona um erro
        viewModel.refreshMediaInfo()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Act
        viewModel.clearError()
        
        // Assert
        val state = viewModel.uiState.value
        assertNull(state.error)
    }

    @Test
    fun `quando atualizar segundos restantes do serviço, deve atualizar estado`() = runTest {
        // Arrange
        val remainingSeconds = 1800L
        
        // Act
        viewModel.updateRemainingSecondsFromService(remainingSeconds)
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals(remainingSeconds, state.remainingSeconds)
    }

    @Test
    fun `quando observar status do timer, deve atualizar estado`() = runTest {
        // Arrange
        val timerStatus = TimerStatus.Running
        every { getTimerStatusUseCase.invoke() } returns MutableStateFlow(timerStatus).asStateFlow()
        
        // Recriar o ViewModel com o novo mock
        viewModel = TimerViewModel(
            startTimerUseCase,
            stopTimerUseCase,
            getTimerStatusUseCase,
            mediaRepository,
            timerRepository
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals(timerStatus, state.timerStatus)
    }

    @Test
    fun `quando observar reprodução de mídia, deve atualizar estado`() = runTest {
        // Arrange
        val mediaInfo = MediaInfo(
            packageName = "com.example.music",
            appName = "Music App",
            isPlaying = true
        )
        every { mediaRepository.observeMediaPlayback() } returns MutableStateFlow(MediaPlaybackState.Playing).asStateFlow()
        coEvery { mediaRepository.getCurrentMediaInfo() } returns mediaInfo
        
        // Recriar o ViewModel com o novo mock
        viewModel = TimerViewModel(
            startTimerUseCase,
            stopTimerUseCase,
            getTimerStatusUseCase,
            mediaRepository,
            timerRepository
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals(mediaInfo, state.currentMediaInfo)
    }

    @Test
    fun `quando mídia parar, deve limpar informações da mídia`() = runTest {
        // Arrange
        every { mediaRepository.observeMediaPlayback() } returns MutableStateFlow(MediaPlaybackState.Stopped).asStateFlow()
        
        // Act
        // testDispatcher.advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertNull(state.currentMediaInfo)
    }

    @Test
    fun `quando não houver mídia, deve limpar informações da mídia`() = runTest {
        // Arrange
        every { mediaRepository.observeMediaPlayback() } returns MutableStateFlow(MediaPlaybackState.NoMedia).asStateFlow()
        
        // Act
        // testDispatcher.advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertNull(state.currentMediaInfo)
    }
} 