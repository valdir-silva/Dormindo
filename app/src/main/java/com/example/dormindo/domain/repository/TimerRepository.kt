package com.example.dormindo.domain.repository

import com.example.dormindo.domain.entity.Timer
import com.example.dormindo.domain.entity.TimerStatus
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de timer
 */
interface TimerRepository {
    /**
     * Inicia o timer
     */
    suspend fun startTimer(timer: Timer): Result<Timer>
    
    /**
     * Para o timer
     */
    suspend fun stopTimer(): Result<Unit>
    
    /**
     * Pausa o timer
     */
    suspend fun pauseTimer(): Result<Unit>
    
    /**
     * Retoma o timer
     */
    suspend fun resumeTimer(): Result<Unit>
    
    /**
     * Obtém o status atual do timer
     */
    fun getTimerStatus(): Flow<TimerStatus>
    
    /**
     * Obtém o timer atual
     */
    fun getCurrentTimer(): Flow<Timer?>
    
    /**
     * Verifica se o timer está ativo
     */
    suspend fun isTimerActive(): Boolean
    
    /**
     * Atualiza os segundos restantes quando receber broadcast do serviço
     */
    fun updateRemainingSeconds(seconds: Long)
} 