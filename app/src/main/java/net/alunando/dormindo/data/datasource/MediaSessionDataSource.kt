package net.alunando.dormindo.data.datasource

import android.content.Context
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.media.session.MediaButtonReceiver
import net.alunando.dormindo.domain.repository.MediaInfo
import net.alunando.dormindo.domain.repository.MediaPlaybackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.view.KeyEvent
import android.content.ComponentName
import net.alunando.dormindo.DormindoNotificationListenerService
import net.alunando.dormindo.R

/**
 * DataSource para controle de mídia usando MediaSession
 */
class MediaSessionDataSource(
    private val context: Context
) {
    
    private val _mediaPlaybackState = MutableStateFlow<MediaPlaybackState>(MediaPlaybackState.NoMedia)
    private var mediaSessionManager: MediaSessionManager? = null
    private var audioManager: AudioManager? = null
    
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }
    }
    
    /**
     * Para a reprodução de mídia atual
     */
    suspend fun stopMediaPlayback(): Result<Unit> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val activeSessions = getActiveMediaSessions()
                if (activeSessions.isNotEmpty()) {
                    // Para a primeira sessão ativa encontrada
                    val controller = activeSessions.first()
                    controller.transportControls.stop()
                    _mediaPlaybackState.value = MediaPlaybackState.Stopped
                    Result.success(Unit)
                } else {
                    // Fallback: tenta pausar usando AudioManager
                    audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
                    audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))
                    _mediaPlaybackState.value = MediaPlaybackState.Stopped
                    Result.success(Unit)
                }
            } else {
                // Fallback para versões anteriores
                audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
                audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))
                _mediaPlaybackState.value = MediaPlaybackState.Stopped
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Pausa a reprodução de mídia atual
     */
    suspend fun pauseMediaPlayback(): Result<Unit> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val activeSessions = getActiveMediaSessions()
                if (activeSessions.isNotEmpty()) {
                    val controller = activeSessions.first()
                    controller.transportControls.pause()
                    _mediaPlaybackState.value = MediaPlaybackState.Paused
                    Result.success(Unit)
                } else {
                    audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
                    audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))
                    _mediaPlaybackState.value = MediaPlaybackState.Paused
                    Result.success(Unit)
                }
            } else {
                audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
                audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))
                _mediaPlaybackState.value = MediaPlaybackState.Paused
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verifica se há mídia sendo reproduzida
     */
    suspend fun isMediaPlaying(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val activeSessions = getActiveMediaSessions()
            activeSessions.any { controller ->
                controller.playbackState?.state == PlaybackState.STATE_PLAYING
            }
        } else {
            // Fallback: verifica se há áudio ativo
            audioManager?.isMusicActive() ?: false
        }
    }
    
    /**
     * Obtém informações da mídia atual
     */
    suspend fun getCurrentMediaInfo(): MediaInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val activeSessions = getActiveMediaSessions()
            activeSessions.firstOrNull { controller ->
                controller.playbackState?.state == PlaybackState.STATE_PLAYING
            }?.let { controller ->
                val metadata = controller.metadata
                val packageName = controller.packageName
                val appName = getAppName(packageName)
                val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
                val title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
                val artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
                
                MediaInfo(
                    packageName = packageName,
                    appName = appName,
                    isPlaying = isPlaying,
                    title = title,
                    artist = artist
                )
            }
        } else {
            // Fallback para versões anteriores
            if (audioManager?.isMusicActive == true) {
                MediaInfo(
                    packageName = context.getString(R.string.media_package_name_unknown),
        appName = context.getString(R.string.media_app_name_unknown),
                    isPlaying = true,
                    title = null,
                    artist = null
                )
            } else {
                null
            }
        }
    }
    
    /**
     * Observa mudanças na reprodução de mídia
     */
    fun observeMediaPlayback(): Flow<MediaPlaybackState> {
        return _mediaPlaybackState.asStateFlow()
    }
    
    /**
     * Atualiza o estado da reprodução
     */
    suspend fun updatePlaybackState() {
        val isPlaying = isMediaPlaying()
        _mediaPlaybackState.update { currentState ->
            when {
                isPlaying -> MediaPlaybackState.Playing
                else -> MediaPlaybackState.NoMedia
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getActiveMediaSessions(): List<MediaController> {
        return try {
            val componentName = ComponentName(context, DormindoNotificationListenerService::class.java)
            val activeSessions = mediaSessionManager?.getActiveSessions(componentName) ?: emptyList()
            activeSessions.filter { controller ->
                controller.playbackState?.state == PlaybackState.STATE_PLAYING ||
                controller.playbackState?.state == PlaybackState.STATE_PAUSED
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    /**
     * Simula mídia sendo reproduzida (para testes)
     */
    fun simulateMediaPlaying() {
        _mediaPlaybackState.value = MediaPlaybackState.Playing
    }
} 