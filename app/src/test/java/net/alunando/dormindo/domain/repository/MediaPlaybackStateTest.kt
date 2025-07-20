package net.alunando.dormindo.domain.repository

import org.junit.Assert.*
import org.junit.Test

class MediaPlaybackStateTest {

    @Test
    fun `deve criar estado Playing`() {
        // Act
        val state = MediaPlaybackState.Playing

        // Assert
        assertTrue(state is MediaPlaybackState.Playing)
    }

    @Test
    fun `deve criar estado Paused`() {
        // Act
        val state = MediaPlaybackState.Paused

        // Assert
        assertTrue(state is MediaPlaybackState.Paused)
    }

    @Test
    fun `deve criar estado Stopped`() {
        // Act
        val state = MediaPlaybackState.Stopped

        // Assert
        assertTrue(state is MediaPlaybackState.Stopped)
    }

    @Test
    fun `deve criar estado NoMedia`() {
        // Act
        val state = MediaPlaybackState.NoMedia

        // Assert
        assertTrue(state is MediaPlaybackState.NoMedia)
    }

    @Test
    fun `deve ser igual quando estado Playing for comparado`() {
        // Arrange
        val state1 = MediaPlaybackState.Playing
        val state2 = MediaPlaybackState.Playing

        // Act & Assert
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `deve ser igual quando estado Paused for comparado`() {
        // Arrange
        val state1 = MediaPlaybackState.Paused
        val state2 = MediaPlaybackState.Paused

        // Act & Assert
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `deve ser igual quando estado Stopped for comparado`() {
        // Arrange
        val state1 = MediaPlaybackState.Stopped
        val state2 = MediaPlaybackState.Stopped

        // Act & Assert
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `deve ser igual quando estado NoMedia for comparado`() {
        // Arrange
        val state1 = MediaPlaybackState.NoMedia
        val state2 = MediaPlaybackState.NoMedia

        // Act & Assert
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `deve ser diferente quando estados forem diferentes`() {
        // Arrange
        val playingState = MediaPlaybackState.Playing
        val pausedState = MediaPlaybackState.Paused
        val stoppedState = MediaPlaybackState.Stopped
        val noMediaState = MediaPlaybackState.NoMedia

        // Act & Assert
        assertNotEquals(playingState, pausedState)
        assertNotEquals(playingState, stoppedState)
        assertNotEquals(playingState, noMediaState)
        assertNotEquals(pausedState, stoppedState)
        assertNotEquals(pausedState, noMediaState)
        assertNotEquals(stoppedState, noMediaState)
    }

    @Test
    fun `deve ter hashCode diferentes para estados diferentes`() {
        // Arrange
        val playingState = MediaPlaybackState.Playing
        val pausedState = MediaPlaybackState.Paused
        val stoppedState = MediaPlaybackState.Stopped
        val noMediaState = MediaPlaybackState.NoMedia

        // Act & Assert
        assertNotEquals(playingState.hashCode(), pausedState.hashCode())
        assertNotEquals(playingState.hashCode(), stoppedState.hashCode())
        assertNotEquals(playingState.hashCode(), noMediaState.hashCode())
        assertNotEquals(pausedState.hashCode(), stoppedState.hashCode())
        assertNotEquals(pausedState.hashCode(), noMediaState.hashCode())
        assertNotEquals(stoppedState.hashCode(), noMediaState.hashCode())
    }

    @Test
    fun `deve ter hashCode iguais para estados iguais`() {
        // Arrange
        val playingState1 = MediaPlaybackState.Playing
        val playingState2 = MediaPlaybackState.Playing
        val pausedState1 = MediaPlaybackState.Paused
        val pausedState2 = MediaPlaybackState.Paused
        val stoppedState1 = MediaPlaybackState.Stopped
        val stoppedState2 = MediaPlaybackState.Stopped
        val noMediaState1 = MediaPlaybackState.NoMedia
        val noMediaState2 = MediaPlaybackState.NoMedia

        // Act & Assert
        assertEquals(playingState1.hashCode(), playingState2.hashCode())
        assertEquals(pausedState1.hashCode(), pausedState2.hashCode())
        assertEquals(stoppedState1.hashCode(), stoppedState2.hashCode())
        assertEquals(noMediaState1.hashCode(), noMediaState2.hashCode())
    }

    @Test
    fun `deve ter toString apropriado para cada estado`() {
        // Act & Assert
        assertTrue(MediaPlaybackState.Playing.toString().contains("Playing"))
        assertTrue(MediaPlaybackState.Paused.toString().contains("Paused"))
        assertTrue(MediaPlaybackState.Stopped.toString().contains("Stopped"))
        assertTrue(MediaPlaybackState.NoMedia.toString().contains("NoMedia"))
    }
} 