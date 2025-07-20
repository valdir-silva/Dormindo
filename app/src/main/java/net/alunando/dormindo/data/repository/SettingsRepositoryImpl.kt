package net.alunando.dormindo.data.repository

import net.alunando.dormindo.domain.entity.TimerSettings
import net.alunando.dormindo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementação mock do SettingsRepository
 */
class SettingsRepositoryImpl : SettingsRepository {
    
    private val _settings = MutableStateFlow(TimerSettings())
    private var _defaultDuration = 30L
    private var _notificationEnabled = true
    
    override fun getSettings(): Flow<TimerSettings> {
        return _settings.asStateFlow()
    }
    
    override suspend fun updateSettings(settings: TimerSettings): Result<Unit> {
        return try {
            _settings.value = settings
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getDefaultDuration(): Long {
        return _defaultDuration
    }
    
    override suspend fun setDefaultDuration(durationMinutes: Long): Result<Unit> {
        return try {
            _defaultDuration = durationMinutes
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isNotificationEnabled(): Boolean {
        return _notificationEnabled
    }
    
    override suspend fun setNotificationEnabled(enabled: Boolean): Result<Unit> {
        return try {
            _notificationEnabled = enabled
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 