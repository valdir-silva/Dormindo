package com.example.dormindo.data.repository

import android.content.Context
import android.content.Intent
import com.example.dormindo.DormindoTimerForegroundService
import com.example.dormindo.data.datasource.NotificationDataSource
import com.example.dormindo.data.datasource.WorkManagerDataSource
import com.example.dormindo.data.datasource.TimerWorkStatus
import com.example.dormindo.domain.entity.Timer
import com.example.dormindo.domain.entity.TimerStatus
import com.example.dormindo.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

/**
 * Implementação do TimerRepository com WorkManager e notificações
 */
class TimerRepositoryImpl(
    private val workManagerDataSource: WorkManagerDataSource,
    private val notificationDataSource: NotificationDataSource
) : TimerRepository {
    
    private val _timerStatus = MutableStateFlow<TimerStatus>(TimerStatus.Idle)
    private val _currentTimer = MutableStateFlow<Timer?>(null)
    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds = _remainingSeconds.asStateFlow()
    
    private fun startForegroundService(context: Context, action: String, durationSeconds: Long? = null) {
        val intent = Intent(context, DormindoTimerForegroundService::class.java).apply {
            this.action = action
            durationSeconds?.let { putExtra(DormindoTimerForegroundService.EXTRA_DURATION, it) }
        }
        context.startForegroundService(intent)
    }
    private fun sendServiceAction(context: Context, action: String) {
        val intent = Intent(context, DormindoTimerForegroundService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }
    
    override suspend fun startTimer(timer: Timer): Result<Timer> {
        return try {
            // Agenda o timer no WorkManager
            val scheduleResult = workManagerDataSource.scheduleTimer(timer.duration.toMinutes())
            
            if (scheduleResult.isSuccess) {
                _currentTimer.value = timer
                _timerStatus.value = TimerStatus.Running
                
                // Inicia o Foreground Service com a duração em segundos
                val seconds = timer.duration.toMinutes() * 60
                _remainingSeconds.value = seconds
                val context = (notificationDataSource as? com.example.dormindo.data.datasource.NotificationDataSource)?.let { it.javaClass.getDeclaredField("context").apply { isAccessible = true }.get(it) as? Context }
                context?.let {
                    startForegroundService(it, DormindoTimerForegroundService.ACTION_START, seconds)
                }
                
                // Removido: notificação simples, agora só foreground
                
                Result.success(timer)
            } else {
                Result.failure(scheduleResult.exceptionOrNull() ?: Exception("Falha ao agendar timer"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun stopTimer(): Result<Unit> {
        return try {
            // Cancela o timer no WorkManager
            val cancelResult = workManagerDataSource.cancelTimer()
            
            if (cancelResult.isSuccess) {
                _currentTimer.value = null
                _timerStatus.value = TimerStatus.Idle
                _remainingSeconds.value = 0L
                
                // Para o Foreground Service
                val context = (notificationDataSource as? com.example.dormindo.data.datasource.NotificationDataSource)?.let { it.javaClass.getDeclaredField("context").apply { isAccessible = true }.get(it) as? Context }
                context?.let {
                    sendServiceAction(it, DormindoTimerForegroundService.ACTION_CANCEL)
                }
                
                // Cancela notificação
                notificationDataSource.cancelTimerNotification()
                
                Result.success(Unit)
            } else {
                Result.failure(cancelResult.exceptionOrNull() ?: Exception("Falha ao cancelar timer"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun pauseTimer(): Result<Unit> {
        return try {
            val currentTimer = _currentTimer.value
            val remainingTime = currentTimer?.remainingTime ?: java.time.Duration.ZERO
            _timerStatus.value = TimerStatus.Paused(remainingTime)
            
            // Pausa o Foreground Service
            val context = (notificationDataSource as? com.example.dormindo.data.datasource.NotificationDataSource)?.let { it.javaClass.getDeclaredField("context").apply { isAccessible = true }.get(it) as? Context }
            context?.let {
                sendServiceAction(it, DormindoTimerForegroundService.ACTION_PAUSE)
            }
            
            // Pausa notificação (mantém mas atualiza texto)
            currentTimer?.let { timer ->
                val seconds = remainingTime.toMinutes() * 60
                _remainingSeconds.value = seconds
                notificationDataSource.updateTimerNotification(seconds, true)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun resumeTimer(): Result<Unit> {
        return try {
            _timerStatus.value = TimerStatus.Running
            
            // Retoma o Foreground Service
            val context = (notificationDataSource as? com.example.dormindo.data.datasource.NotificationDataSource)?.let { it.javaClass.getDeclaredField("context").apply { isAccessible = true }.get(it) as? Context }
            context?.let {
                sendServiceAction(it, DormindoTimerForegroundService.ACTION_RESUME)
            }
            
            // Atualiza notificação
            val currentTimer = _currentTimer.value
            currentTimer?.let { timer ->
                val seconds = timer.duration.toMinutes() * 60
                _remainingSeconds.value = seconds
                notificationDataSource.updateTimerNotification(seconds, false)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getTimerStatus(): Flow<TimerStatus> {
        return combine(
            _timerStatus,
            workManagerDataSource.observeTimerStatus()
        ) { localStatus, workStatus ->
            when (workStatus) {
                is TimerWorkStatus.Idle -> TimerStatus.Idle
                is TimerWorkStatus.Scheduled -> TimerStatus.Running
                is TimerWorkStatus.Running -> TimerStatus.Running
                is TimerWorkStatus.Completed -> {
                    // Mostra notificação de conclusão
//                    notificationDataSource.showTimerCompletedNotification()
                    TimerStatus.Idle
                }
                is TimerWorkStatus.Cancelled -> TimerStatus.Idle
                is TimerWorkStatus.Failed -> TimerStatus.Idle
            }
        }
    }
    
    override fun getCurrentTimer(): Flow<Timer?> {
        return _currentTimer.asStateFlow()
    }
    
    override suspend fun isTimerActive(): Boolean {
        return workManagerDataSource.isTimerActive()
    }

    // Adiciona1uto ao timer atual
    suspend fun addOneMinuteToTimer() {
        val currentTimer = _currentTimer.value
        if (currentTimer != null && _timerStatus.value is TimerStatus.Running) {
            // Envia ação para o serviço de foreground adicionar 1 minuto
            val context = (notificationDataSource as? com.example.dormindo.data.datasource.NotificationDataSource)?.let { it.javaClass.getDeclaredField("context").apply { isAccessible = true }.get(it) as? Context }
            context?.let {
                sendServiceAction(it, DormindoTimerForegroundService.ACTION_ADD_MINUTE)
            }
        }
    }

    // Atualiza os segundos restantes quando receber broadcast do serviço
    override fun updateRemainingSeconds(seconds: Long) {
        _remainingSeconds.value = seconds
    }
} 