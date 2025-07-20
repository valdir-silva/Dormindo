package com.example.dormindo.data.datasource

import android.content.Context
import androidx.work.*
import com.example.dormindo.data.worker.TimerWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

/**
 * DataSource para gerenciar tarefas em background usando WorkManager
 */
class WorkManagerDataSource(
    private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    private val _timerWorkStatus = MutableStateFlow<TimerWorkStatus>(TimerWorkStatus.Idle)
    
    /**
     * Agenda o timer para executar em background
     */
    suspend fun scheduleTimer(durationMinutes: Long): Result<Unit> {
        return try {
            // Cancela qualquer timer anterior
            cancelTimer()
            
            // Cria a requisição de trabalho
            val timerWorkRequest = OneTimeWorkRequestBuilder<TimerWorker>()
                .setInputData(workDataOf(
                    "duration_minutes" to durationMinutes
                ))
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    10000L, // 10 segundos
                    TimeUnit.MILLISECONDS
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()
            
            // Agenda o trabalho
            workManager.enqueueUniqueWork(
                TIMER_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                timerWorkRequest
            )
            
            _timerWorkStatus.value = TimerWorkStatus.Scheduled(durationMinutes)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cancela o timer em execução
     */
    suspend fun cancelTimer(): Result<Unit> {
        return try {
            workManager.cancelUniqueWork(TIMER_WORK_NAME)
            _timerWorkStatus.value = TimerWorkStatus.Cancelled
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verifica se o timer está ativo
     */
    suspend fun isTimerActive(): Boolean {
        return try {
            val workInfo = workManager.getWorkInfosForUniqueWork(TIMER_WORK_NAME).get()
            workInfo.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obtém o status atual do timer
     */
    suspend fun getTimerStatus(): TimerWorkStatus {
        return try {
            val workInfo = workManager.getWorkInfosForUniqueWork(TIMER_WORK_NAME).get()
            val activeWork = workInfo.firstOrNull { 
                it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED 
            }
            
            when {
                activeWork != null -> {
                    val durationMinutes = activeWork.outputData.getLong("duration_minutes", 0)
                    TimerWorkStatus.Scheduled(durationMinutes)
                }
                workInfo.any { it.state == WorkInfo.State.SUCCEEDED } -> {
                    TimerWorkStatus.Completed
                }
                workInfo.any { it.state == WorkInfo.State.CANCELLED } -> {
                    TimerWorkStatus.Cancelled
                }
                workInfo.any { it.state == WorkInfo.State.FAILED } -> {
                    TimerWorkStatus.Failed
                }
                else -> TimerWorkStatus.Idle
            }
        } catch (e: Exception) {
            TimerWorkStatus.Idle
        }
    }
    
    /**
     * Observa mudanças no status do timer
     */
    fun observeTimerStatus(): Flow<TimerWorkStatus> {
        return _timerWorkStatus.asStateFlow()
    }
    
    /**
     * Atualiza o status do timer
     */
    suspend fun updateTimerStatus() {
        val status = getTimerStatus()
        _timerWorkStatus.value = status
    }
    
    companion object {
        private const val TIMER_WORK_NAME = "dormindo_timer_work"
    }
}

/**
 * Status do timer em background
 */
sealed class TimerWorkStatus {
    object Idle : TimerWorkStatus()
    data class Scheduled(val durationMinutes: Long) : TimerWorkStatus()
    object Running : TimerWorkStatus()
    object Completed : TimerWorkStatus()
    object Cancelled : TimerWorkStatus()
    object Failed : TimerWorkStatus()
} 