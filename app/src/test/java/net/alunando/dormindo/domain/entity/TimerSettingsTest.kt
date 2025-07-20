package net.alunando.dormindo.domain.entity

import org.junit.Assert.*
import org.junit.Test
import java.time.Duration

class TimerSettingsTest {

    @Test
    fun `deve criar TimerSettings com valores padrão corretos`() {
        // Act
        val settings = TimerSettings()

        // Assert
        assertEquals(Duration.ofMinutes(30), settings.defaultDuration)
        assertTrue(settings.showNotification)
        assertFalse(settings.vibrateOnComplete)
        assertFalse(settings.autoStart)
        assertEquals(TimerSettings.Theme.DARK, settings.theme)
    }

    @Test
    fun `deve criar TimerSettings com valores customizados`() {
        // Arrange
        val defaultDuration = Duration.ofMinutes(45)
        val showNotification = false
        val vibrateOnComplete = true
        val autoStart = true
        val theme = TimerSettings.Theme.LIGHT

        // Act
        val settings = TimerSettings(
            defaultDuration = defaultDuration,
            showNotification = showNotification,
            vibrateOnComplete = vibrateOnComplete,
            autoStart = autoStart,
            theme = theme
        )

        // Assert
        assertEquals(defaultDuration, settings.defaultDuration)
        assertEquals(showNotification, settings.showNotification)
        assertEquals(vibrateOnComplete, settings.vibrateOnComplete)
        assertEquals(autoStart, settings.autoStart)
        assertEquals(theme, settings.theme)
    }

    @Test
    fun `deve criar TimerSettings com diferentes durações padrão`() {
        // Arrange
        val duration1 = Duration.ofMinutes(15)
        val duration2 = Duration.ofHours(1)
        val duration3 = Duration.ofSeconds(30)

        // Act
        val settings1 = TimerSettings(defaultDuration = duration1)
        val settings2 = TimerSettings(defaultDuration = duration2)
        val settings3 = TimerSettings(defaultDuration = duration3)

        // Assert
        assertEquals(duration1, settings1.defaultDuration)
        assertEquals(duration2, settings2.defaultDuration)
        assertEquals(duration3, settings3.defaultDuration)
    }

    @Test
    fun `deve criar TimerSettings com diferentes temas`() {
        // Act
        val lightTheme = TimerSettings(theme = TimerSettings.Theme.LIGHT)
        val darkTheme = TimerSettings(theme = TimerSettings.Theme.DARK)
        val systemTheme = TimerSettings(theme = TimerSettings.Theme.SYSTEM)

        // Assert
        assertEquals(TimerSettings.Theme.LIGHT, lightTheme.theme)
        assertEquals(TimerSettings.Theme.DARK, darkTheme.theme)
        assertEquals(TimerSettings.Theme.SYSTEM, systemTheme.theme)
    }

    @Test
    fun `deve ser igual quando TimerSettings tiverem mesmos valores`() {
        // Arrange
        val settings1 = TimerSettings(
            defaultDuration = Duration.ofMinutes(30),
            showNotification = true,
            vibrateOnComplete = false,
            autoStart = true,
            theme = TimerSettings.Theme.DARK
        )
        val settings2 = TimerSettings(
            defaultDuration = Duration.ofMinutes(30),
            showNotification = true,
            vibrateOnComplete = false,
            autoStart = true,
            theme = TimerSettings.Theme.DARK
        )

        // Act & Assert
        assertEquals(settings1, settings2)
        assertEquals(settings1.hashCode(), settings2.hashCode())
    }

    @Test
    fun `deve ser diferente quando TimerSettings tiverem valores diferentes`() {
        // Arrange
        val settings1 = TimerSettings(
            defaultDuration = Duration.ofMinutes(30),
            showNotification = true,
            theme = TimerSettings.Theme.DARK
        )
        val settings2 = TimerSettings(
            defaultDuration = Duration.ofMinutes(45),
            showNotification = true,
            theme = TimerSettings.Theme.DARK
        )

        // Act & Assert
        assertNotEquals(settings1, settings2)
        assertNotEquals(settings1.hashCode(), settings2.hashCode())
    }

    @Test
    fun `deve copiar TimerSettings com novos valores`() {
        // Arrange
        val originalSettings = TimerSettings(
            defaultDuration = Duration.ofMinutes(30),
            showNotification = true,
            vibrateOnComplete = false,
            autoStart = false,
            theme = TimerSettings.Theme.DARK
        )

        // Act
        val copiedSettings = originalSettings.copy(
            defaultDuration = Duration.ofMinutes(45),
            showNotification = false,
            vibrateOnComplete = true
        )

        // Assert
        assertEquals(Duration.ofMinutes(45), copiedSettings.defaultDuration)
        assertFalse(copiedSettings.showNotification)
        assertTrue(copiedSettings.vibrateOnComplete)
        assertFalse(copiedSettings.autoStart) // Mantém valor original
        assertEquals(TimerSettings.Theme.DARK, copiedSettings.theme) // Mantém valor original
    }

    @Test
    fun `deve testar todos os valores do enum Theme`() {
        // Act & Assert
        assertEquals(3, TimerSettings.Theme.values().size)
        assertTrue(TimerSettings.Theme.values().contains(TimerSettings.Theme.LIGHT))
        assertTrue(TimerSettings.Theme.values().contains(TimerSettings.Theme.DARK))
        assertTrue(TimerSettings.Theme.values().contains(TimerSettings.Theme.SYSTEM))
    }

    @Test
    fun `deve criar TimerSettings com configurações para uso noturno`() {
        // Arrange
        val nightSettings = TimerSettings(
            defaultDuration = Duration.ofMinutes(60),
            showNotification = false,
            vibrateOnComplete = true,
            autoStart = true,
            theme = TimerSettings.Theme.DARK
        )

        // Assert
        assertEquals(Duration.ofMinutes(60), nightSettings.defaultDuration)
        assertFalse(nightSettings.showNotification)
        assertTrue(nightSettings.vibrateOnComplete)
        assertTrue(nightSettings.autoStart)
        assertEquals(TimerSettings.Theme.DARK, nightSettings.theme)
    }

    @Test
    fun `deve criar TimerSettings com configurações para uso diurno`() {
        // Arrange
        val daySettings = TimerSettings(
            defaultDuration = Duration.ofMinutes(15),
            showNotification = true,
            vibrateOnComplete = false,
            autoStart = false,
            theme = TimerSettings.Theme.LIGHT
        )

        // Assert
        assertEquals(Duration.ofMinutes(15), daySettings.defaultDuration)
        assertTrue(daySettings.showNotification)
        assertFalse(daySettings.vibrateOnComplete)
        assertFalse(daySettings.autoStart)
        assertEquals(TimerSettings.Theme.LIGHT, daySettings.theme)
    }
} 