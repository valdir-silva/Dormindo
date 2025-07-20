package net.alunando.dormindo.data.repository

import net.alunando.dormindo.data.datasource.MediaSessionDataSource
import net.alunando.dormindo.domain.repository.MediaRepository
import net.alunando.dormindo.domain.repository.MediaInfo
import net.alunando.dormindo.domain.repository.MediaPlaybackState
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