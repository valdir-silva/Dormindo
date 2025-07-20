package com.example.dormindo.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de mídia
 */
interface MediaRepository {
    /**
     * Para a reprodução de mídia atual
     */
    suspend fun stopMediaPlayback(): Result<Unit>
    
    /**
     * Pausa a reprodução de mídia atual
     */
    suspend fun pauseMediaPlayback(): Result<Unit>
    
    /**
     * Verifica se há mídia sendo reproduzida
     */
    suspend fun isMediaPlaying(): Boolean
    
    /**
     * Obtém informações da mídia atual
     */
    suspend fun getCurrentMediaInfo(): MediaInfo?
    
    /**
     * Observa mudanças na reprodução de mídia
     */
    fun observeMediaPlayback(): Flow<MediaPlaybackState>
}

/**
 * Informações da mídia atual
 */
data class MediaInfo(
    val packageName: String,
    val appName: String,
    val isPlaying: Boolean,
    val title: String? = null,
    val artist: String? = null
)

/**
 * Estado da reprodução de mídia
 */
sealed class MediaPlaybackState {
    object Playing : MediaPlaybackState()
    object Paused : MediaPlaybackState()
    object Stopped : MediaPlaybackState()
    object NoMedia : MediaPlaybackState()
} 