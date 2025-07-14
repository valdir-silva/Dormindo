package com.example.dormindo.data.repository

import com.example.dormindo.data.datasource.MediaSessionDataSource
import com.example.dormindo.domain.repository.MediaRepository
import com.example.dormindo.domain.repository.MediaInfo
import com.example.dormindo.domain.repository.MediaPlaybackState
import kotlinx.coroutines.flow.Flow

/**
 * Implementação real do MediaRepository usando MediaSession
 */
class MediaRepositoryImpl(
    private val mediaSessionDataSource: MediaSessionDataSource
) : MediaRepository {
    
    override suspend fun stopMediaPlayback(): Result<Unit> {
        return mediaSessionDataSource.stopMediaPlayback()
    }
    
    override suspend fun pauseMediaPlayback(): Result<Unit> {
        return mediaSessionDataSource.pauseMediaPlayback()
    }
    
    override suspend fun isMediaPlaying(): Boolean {
        return mediaSessionDataSource.isMediaPlaying()
    }
    
    override suspend fun getCurrentMediaInfo(): MediaInfo? {
        return mediaSessionDataSource.getCurrentMediaInfo()
    }
    
    override fun observeMediaPlayback(): Flow<MediaPlaybackState> {
        return mediaSessionDataSource.observeMediaPlayback()
    }
    
    /**
     * Atualiza o estado da reprodução
     */
    suspend fun updatePlaybackState() {
        mediaSessionDataSource.updatePlaybackState()
    }
} 