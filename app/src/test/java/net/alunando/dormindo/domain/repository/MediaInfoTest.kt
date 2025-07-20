package net.alunando.dormindo.domain.repository

import org.junit.Assert.*
import org.junit.Test

// Supondo que MediaInfo está em com.example.dormindo.domain.repository
// Ajuste o import abaixo se necessário
// import com.example.dormindo.domain.repository.MediaInfo

class MediaInfoTest {
    @Test
    fun `deve criar MediaInfo com todos os campos`() {
        val packageName = "com.example.music"
        val appName = "Music App"
        val isPlaying = true
        val title = "Test Song"
        val artist = "Test Artist"

        val mediaInfo = MediaInfo(
            packageName = packageName,
            appName = appName,
            isPlaying = isPlaying,
            title = title,
            artist = artist
        )

        assertEquals(packageName, mediaInfo.packageName)
        assertEquals(appName, mediaInfo.appName)
        assertEquals(isPlaying, mediaInfo.isPlaying)
        assertEquals(title, mediaInfo.title)
        assertEquals(artist, mediaInfo.artist)
    }

    @Test
    fun `deve criar MediaInfo apenas com campos obrigatórios`() {
        val packageName = "com.example.music"
        val appName = "Music App"
        val isPlaying = false

        val mediaInfo = MediaInfo(
            packageName = packageName,
            appName = appName,
            isPlaying = isPlaying
        )

        assertEquals(packageName, mediaInfo.packageName)
        assertEquals(appName, mediaInfo.appName)
        assertEquals(isPlaying, mediaInfo.isPlaying)
        assertNull(mediaInfo.title)
        assertNull(mediaInfo.artist)
    }

    @Test
    fun `MediaInfo com mesmos valores deve ser igual`() {
        val m1 = MediaInfo("com.example.music", "Music App", true, "Song", "Artist")
        val m2 = MediaInfo("com.example.music", "Music App", true, "Song", "Artist")
        assertEquals(m1, m2)
        assertEquals(m1.hashCode(), m2.hashCode())
    }

    @Test
    fun `MediaInfo com valores diferentes deve ser diferente`() {
        val m1 = MediaInfo("com.example.music", "Music App", true, "Song 1", "Artist 1")
        val m2 = MediaInfo("com.example.music", "Music App", false, "Song 2", "Artist 2")
        assertNotEquals(m1, m2)
    }

    @Test
    fun `MediaInfo com title e artist nulos deve ser igual`() {
        val m1 = MediaInfo("com.example.music", "Music App", true, null, null)
        val m2 = MediaInfo("com.example.music", "Music App", true, null, null)
        assertEquals(m1, m2)
    }
} 