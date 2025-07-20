package com.example.dormindo.data.repository

import com.example.dormindo.domain.entity.TimerSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsRepository: SettingsRepositoryImpl

    @Before
    fun setup() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        settingsRepository = SettingsRepositoryImpl()
    }
    
    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `quando inicializado, deve ter configurações padrão`() = runTest {
        // Act
        val settings = settingsRepository.getSettings().first()

        // Assert
        assertEquals(Duration.ofMinutes(30), settings.defaultDuration)
        assertTrue(settings.showNotification)
        assertFalse(settings.vibrateOnComplete)
        assertFalse(settings.autoStart)
        assertEquals(TimerSettings.Theme.DARK, settings.theme)
    }

    @Test
    fun `quando updateSettings for chamado, deve atualizar configurações`() = runTest {
        // Arrange
        val newSettings = TimerSettings(
            defaultDuration = Duration.ofMinutes(45),
            showNotification = false,
            vibrateOnComplete = true,
            autoStart = true,
            theme = TimerSettings.Theme.LIGHT
        )

        // Act
        val result = settingsRepository.updateSettings(newSettings)
        val updatedSettings = settingsRepository.getSettings().first()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(newSettings, updatedSettings)
    }

    @Test
    fun `quando getDefaultDuration for chamado, deve retornar duração padrão`() = runTest {
        // Act
        val duration = settingsRepository.getDefaultDuration()

        // Assert
        assertEquals(30, duration)
    }

    @Test
    fun `quando setDefaultDuration for chamado, deve atualizar duração padrão`() = runTest {
        // Arrange
        val newDuration = 60L

        // Act
        val result = settingsRepository.setDefaultDuration(newDuration)
        val updatedDuration = settingsRepository.getDefaultDuration()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(newDuration, updatedDuration)
    }

    @Test
    fun `quando isNotificationEnabled for chamado, deve retornar status das notificações`() = runTest {
        // Act
        val isEnabled = settingsRepository.isNotificationEnabled()

        // Assert
        assertTrue(isEnabled)
    }

    @Test
    fun `quando setNotificationEnabled for chamado com true, deve habilitar notificações`() = runTest {
        // Act
        val result = settingsRepository.setNotificationEnabled(true)
        val isEnabled = settingsRepository.isNotificationEnabled()

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(isEnabled)
    }

    @Test
    fun `quando setNotificationEnabled for chamado com false, deve desabilitar notificações`() = runTest {
        // Act
        val result = settingsRepository.setNotificationEnabled(false)
        val isEnabled = settingsRepository.isNotificationEnabled()

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(isEnabled)
    }

    @Test
    fun `quando múltiplas atualizações forem feitas, deve manter estado consistente`() = runTest {
        // Arrange
        val settings1 = TimerSettings(
            defaultDuration = Duration.ofMinutes(15),
            showNotification = false,
            theme = TimerSettings.Theme.LIGHT
        )
        val settings2 = TimerSettings(
            defaultDuration = Duration.ofMinutes(60),
            showNotification = true,
            vibrateOnComplete = true,
            theme = TimerSettings.Theme.DARK
        )

        // Act
        settingsRepository.updateSettings(settings1)
        val result1 = settingsRepository.getSettings().first()
        
        settingsRepository.updateSettings(settings2)
        val result2 = settingsRepository.getSettings().first()

        // Assert
        assertEquals(settings1, result1)
        assertEquals(settings2, result2)
    }

    @Test
    fun `quando setDefaultDuration for chamado múltiplas vezes, deve manter último valor`() = runTest {
        // Arrange
        val durations = listOf(15, 30L, 45, 60L)

        // Act & Assert
        durations.forEach { duration ->
            val result = settingsRepository.setDefaultDuration(duration)
            val currentDuration = settingsRepository.getDefaultDuration()
            
            assertTrue(result.isSuccess)
            assertEquals(duration, currentDuration)
        }
    }

    @Test
    fun `quando setNotificationEnabled for chamado múltiplas vezes, deve alternar corretamente`() = runTest {
        // Act & Assert
        // Primeira vez: true (padrão)
        assertTrue(settingsRepository.isNotificationEnabled())
        
        // Segunda vez: false
        settingsRepository.setNotificationEnabled(false)
        assertFalse(settingsRepository.isNotificationEnabled())
        
        // Terceira vez: true
        settingsRepository.setNotificationEnabled(true)
        assertTrue(settingsRepository.isNotificationEnabled())
        
        // Quarta vez: false
        settingsRepository.setNotificationEnabled(false)
        assertFalse(settingsRepository.isNotificationEnabled())
    }

    @Test
    fun `quando updateSettings for chamado com configurações parciais, deve manter valores não especificados`() = runTest {
        // Arrange
        val originalSettings = settingsRepository.getSettings().first()
        val partialSettings = TimerSettings(
            defaultDuration = Duration.ofMinutes(45),
            theme = TimerSettings.Theme.LIGHT
        )

        // Act
        settingsRepository.updateSettings(partialSettings)
        val updatedSettings = settingsRepository.getSettings().first()

        // Assert
        assertEquals(Duration.ofMinutes(45), updatedSettings.defaultDuration)
        assertEquals(TimerSettings.Theme.LIGHT, updatedSettings.theme)
        assertEquals(originalSettings.showNotification, updatedSettings.showNotification)
        assertEquals(originalSettings.vibrateOnComplete, updatedSettings.vibrateOnComplete)
        assertEquals(originalSettings.autoStart, updatedSettings.autoStart)
    }

    @Test
    fun `quando getSettings for observado, deve emitir mudanças`() = runTest {
        // Arrange
        val newSettings = TimerSettings(
            defaultDuration = Duration.ofMinutes(60),
            showNotification = false,
            vibrateOnComplete = true,
            autoStart = true,
            theme = TimerSettings.Theme.SYSTEM
        )

        // Act
        val settingsFlow = settingsRepository.getSettings()
        val initialSettings = settingsFlow.first()
        
        settingsRepository.updateSettings(newSettings)
        val updatedSettings = settingsFlow.first()

        // Assert
        assertFalse(initialSettings == updatedSettings)
        assertEquals(newSettings, updatedSettings)
    }

    @Test
    fun `quando setDefaultDuration for chamado com valor zero, deve aceitar`() = runTest {
        // Act
        val result = settingsRepository.setDefaultDuration(0L)
        val duration = settingsRepository.getDefaultDuration()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0L, duration)
    }

    @Test
    fun `quando setDefaultDuration for chamado com valor negativo, deve aceitar`() = runTest {
        // Act
        val result = settingsRepository.setDefaultDuration(-10L)
        val duration = settingsRepository.getDefaultDuration()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(-10, duration)
    }

    @Test
    fun `quando updateSettings for chamado com configurações extremas, deve aceitar`() = runTest {
        // Arrange
        val extremeSettings = TimerSettings(
            defaultDuration = Duration.ofHours(24),
            showNotification = false,
            vibrateOnComplete = true,
            autoStart = true,
            theme = TimerSettings.Theme.SYSTEM
        )

        // Act
        val result = settingsRepository.updateSettings(extremeSettings)
        val settings = settingsRepository.getSettings().first()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(extremeSettings, settings)
    }
} 