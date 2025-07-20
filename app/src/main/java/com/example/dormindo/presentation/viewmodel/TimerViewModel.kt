package com.example.dormindo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormindo.domain.entity.Timer
import com.example.dormindo.domain.entity.TimerStatus
import com.example.dormindo.domain.repository.MediaInfo
import com.example.dormindo.domain.repository.MediaRepository
import com.example.dormindo.domain.repository.TimerRepository
import com.example.dormindo.domain.usecase.GetTimerStatusUseCase
import com.example.dormindo.domain.usecase.StartTimerUseCase
import com.example.dormindo.domain.usecase.StopTimerUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Duration
import android.util.Log

/**
 * ViewModel para a tela do timer
 */
class TimerViewModel(
    private val startTimerUseCase: StartTimerUseCase,
    private val stopTimerUseCase: StopTimerUseCase,
    private val getTimerStatusUseCase: GetTimerStatusUseCase,
    private val mediaRepository: MediaRepository,
    private val timerRepository: TimerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()
    
    init {
        observeTimerStatus()
        observeMediaInfo()
        observeRemainingSeconds()
    }

    /**
     * Inicia o timer
     */
    fun startTimer(durationMinutes: Long) {
        // Protege contra múltiplas execuções simultâneas
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.currentTimer != null) {
            // Já está processando ou já existe um timer ativo
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val duration = java.time.Duration.ofMinutes(durationMinutes)
            val isPlaying = mediaRepository.isMediaPlaying()
            if (!isPlaying) {
                Log.w("TimerViewModel", "Tentativa de iniciar timer sem mídia tocando.")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Nenhuma mídia está sendo reproduzida. Abra um app de música e tente novamente."
                )
                return@launch
            }
            
            val result = startTimerUseCase(duration)
            result.fold(
                onSuccess = { timer ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentTimer = timer,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Erro ao iniciar timer"
                    )
                }
            )
        }
    }

    /**
     * Para o timer
     */
    fun stopTimer() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = stopTimerUseCase()
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentTimer = null,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Erro ao parar timer"
                    )
                }
            )
        }
    }

    /**
     * Observa o status do timer
     */
    private fun observeTimerStatus() {
        viewModelScope.launch {
            getTimerStatusUseCase().collect { status ->
                _uiState.value = _uiState.value.copy(
                    timerStatus = status
                )
            }
        }
    }
    
    /**
     * Observa informações da mídia
     */
    private fun observeMediaInfo() {
        viewModelScope.launch {
            mediaRepository.observeMediaPlayback().collect { playbackState ->
                when (playbackState) {
                    is com.example.dormindo.domain.repository.MediaPlaybackState.Playing -> {
                        // Atualiza informações da mídia quando está reproduzindo
                        val mediaInfo = mediaRepository.getCurrentMediaInfo()
                        _uiState.value = _uiState.value.copy(
                            currentMediaInfo = mediaInfo
                        )
                    }
                    is com.example.dormindo.domain.repository.MediaPlaybackState.Paused -> {
                        _uiState.value = _uiState.value.copy(
                            currentMediaInfo = _uiState.value.currentMediaInfo?.copy(isPlaying = false)
                        )
                    }
                    is com.example.dormindo.domain.repository.MediaPlaybackState.Stopped -> {
                        _uiState.value = _uiState.value.copy(
                            currentMediaInfo = null
                        )
                    }
                    is com.example.dormindo.domain.repository.MediaPlaybackState.NoMedia -> {
                        _uiState.value = _uiState.value.copy(
                            currentMediaInfo = null
                        )
                    }
                }
            }
        }
    }
    
    private fun observeRemainingSeconds() {
        viewModelScope.launch {
            (timerRepository as? com.example.dormindo.data.repository.TimerRepositoryImpl)?.remainingSeconds?.collectLatest { seconds ->
                _uiState.value = _uiState.value.copy(remainingSeconds = seconds)
            }
        }
    }

    /**
     * Atualiza informações da mídia manualmente
     */
    fun refreshMediaInfo() {
        viewModelScope.launch {
            val mediaInfo = mediaRepository.getCurrentMediaInfo()
            if (mediaInfo == null) {
                Log.w("TimerViewModel", "Nenhuma mídia detectada ao atualizar.")
                _uiState.value = _uiState.value.copy(
                    currentMediaInfo = null,
                    error = "Nenhuma mídia detectada. Certifique-se de que um app de música está tocando e que a permissão foi concedida."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    currentMediaInfo = mediaInfo,
                    error = null
                )
            }
        }
    }

    /**
     * Limpa o erro
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun updateRemainingSecondsFromService(seconds: Long) {
        _uiState.value = _uiState.value.copy(remainingSeconds = seconds)
        // Também atualiza o repositório para manter sincronização
        viewModelScope.launch {
            timerRepository.updateRemainingSeconds(seconds)
        }
    }

    fun updatePauseStateFromService(seconds: Long, isPaused: Boolean) {
        Log.d("TimerViewModel", "Recebido broadcast: $seconds segundos, pausado: $isPaused")
        _uiState.value = _uiState.value.copy(
            remainingSeconds = seconds,
            timerStatus = if (isPaused) TimerStatus.Paused(java.time.Duration.ofSeconds(seconds)) else TimerStatus.Running
        )
        // Também atualiza o repositório para manter sincronização
        viewModelScope.launch {
            timerRepository.updateRemainingSeconds(seconds)
        }
    }
}

/**
 * Estado da UI do timer
 */
data class TimerUiState(
    val isLoading: Boolean = false,
    val timerStatus: TimerStatus = TimerStatus.Idle,
    val currentTimer: Timer? = null,
    val currentMediaInfo: MediaInfo? = null,
    val error: String? = null,
    val remainingSeconds: Long = 0
) 