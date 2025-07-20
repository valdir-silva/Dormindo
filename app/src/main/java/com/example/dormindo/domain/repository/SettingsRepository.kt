package com.example.dormindo.domain.repository

import com.example.dormindo.domain.entity.TimerSettings
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de configurações
 */
interface SettingsRepository {
    /**
     * Obtém as configurações atuais
     */
    fun getSettings(): Flow<TimerSettings>
    
    /**
     * Atualiza as configurações
     */
    suspend fun updateSettings(settings: TimerSettings): Result<Unit>
    
    /**
     * Obtém a duração padrão do timer
     */
    suspend fun getDefaultDuration(): Long
    
    /**
     * Define a duração padrão do timer
     */
    suspend fun setDefaultDuration(durationMinutes: Long): Result<Unit>
    
    /**
     * Verifica se as notificações estão habilitadas
     */
    suspend fun isNotificationEnabled(): Boolean
    
    /**
     * Habilita/desabilita notificações
     */
    suspend fun setNotificationEnabled(enabled: Boolean): Result<Unit>
} 