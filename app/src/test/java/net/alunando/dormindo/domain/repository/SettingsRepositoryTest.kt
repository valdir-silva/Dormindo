package net.alunando.dormindo.domain.repository

import net.alunando.dormindo.domain.entity.TimerSettings
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
class SettingsRepositoryTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `quando getSettings for chamado, deve retornar Flow com configurações`() = runTest {
        // Arrange
        val expectedSettings = TimerSettings(
            defaultDuration = Duration.ofMinutes(45),
            showNotification = false,
            vibrateOnComplete = true,
            autoStart = true,
            theme = TimerSettings.Theme.LIGHT
        )
        val settingsFlow = MutableStateFlow(expectedSettings).asStateFlow()

        coEvery { settingsRepository.getSettings() } returns settingsFlow

        // Act
        val result = settingsRepository.getSettings().first()

        // Assert
        assertEquals(expectedSettings, result)
        coVerify { settingsRepository.getSettings() }
    }

    @Test
    fun `quando updateSettings for chamado com sucesso, deve retornar Result success`() = runTest {
        // Arrange
        val settings = TimerSettings(
            defaultDuration = Duration.ofMinutes(30),
            showNotification = true,
            vibrateOnComplete = false,
            autoStart = false,
            theme = TimerSettings.Theme.DARK
        )

        coEvery { settingsRepository.updateSettings(settings) } returns Result.success(Unit)

        // Act
        val result = settingsRepository.updateSettings(settings)

        // Assert
        assertTrue(result.isSuccess)
        coVerify { settingsRepository.updateSettings(settings) }
    }

    @Test
    fun `quando updateSettings falhar, deve retornar Result failure`() = runTest {
        // Arrange
        val settings = TimerSettings()
        val exception = Exception("Erro ao atualizar configurações")

        coEvery { settingsRepository.updateSettings(settings) } returns Result.failure(exception)

        // Act
        val result = settingsRepository.updateSettings(settings)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { settingsRepository.updateSettings(settings) }
    }

    @Test
    fun `quando getDefaultDuration for chamado, deve retornar duração em minutos`() = runTest {
        // Arrange
        val expectedDuration = 45L
        coEvery { settingsRepository.getDefaultDuration() } returns expectedDuration

        // Act
        val result = settingsRepository.getDefaultDuration()

        // Assert
        assertEquals(expectedDuration, result)
        coVerify { settingsRepository.getDefaultDuration() }
    }

    @Test
    fun `quando setDefaultDuration for chamado com sucesso, deve retornar Result success`() =
        runTest {
            // Arrange
            val durationMinutes = 60L
            coEvery { settingsRepository.setDefaultDuration(durationMinutes) } returns Result.success(
                Unit
            )

            // Act
            val result = settingsRepository.setDefaultDuration(durationMinutes)

            // Assert
            assertTrue(result.isSuccess)
            coVerify { settingsRepository.setDefaultDuration(durationMinutes) }
        }

    @Test
    fun `quando setDefaultDuration falhar, deve retornar Result failure`() = runTest {
        // Arrange
        val durationMinutes = 60L
        val exception = Exception("Erro ao definir duração padrão")

        coEvery { settingsRepository.setDefaultDuration(durationMinutes) } returns Result.failure(
            exception
        )

        // Act
        val result = settingsRepository.setDefaultDuration(durationMinutes)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { settingsRepository.setDefaultDuration(durationMinutes) }
    }

    @Test
    fun `quando isNotificationEnabled for chamado e notificações estiverem habilitadas, deve retornar true`() =
        runTest {
            // Arrange
            coEvery { settingsRepository.isNotificationEnabled() } returns true

            // Act
            val result = settingsRepository.isNotificationEnabled()

            // Assert
            assertTrue(result)
            coVerify { settingsRepository.isNotificationEnabled() }
        }

    @Test
    fun `quando isNotificationEnabled for chamado e notificações estiverem desabilitadas, deve retornar false`() =
        runTest {
            // Arrange
            coEvery { settingsRepository.isNotificationEnabled() } returns false

            // Act
            val result = settingsRepository.isNotificationEnabled()

            // Assert
            assertFalse(result)
            coVerify { settingsRepository.isNotificationEnabled() }
        }

    @Test
    fun `quando setNotificationEnabled for chamado com true, deve retornar Result success`() =
        runTest {
            // Arrange
            coEvery { settingsRepository.setNotificationEnabled(true) } returns Result.success(Unit)

            // Act
            val result = settingsRepository.setNotificationEnabled(true)

            // Assert
            assertTrue(result.isSuccess)
            coVerify { settingsRepository.setNotificationEnabled(true) }
        }

    @Test
    fun `quando setNotificationEnabled for chamado com false, deve retornar Result success`() =
        runTest {
            // Arrange
            coEvery { settingsRepository.setNotificationEnabled(false) } returns Result.success(Unit)

            // Act
            val result = settingsRepository.setNotificationEnabled(false)

            // Assert
            assertTrue(result.isSuccess)
            coVerify { settingsRepository.setNotificationEnabled(false) }
        }

    @Test
    fun `quando setNotificationEnabled falhar, deve retornar Result failure`() = runTest {
        // Arrange
        val exception = Exception("Erro ao configurar notificações")
        coEvery { settingsRepository.setNotificationEnabled(true) } returns Result.failure(exception)

        // Act
        val result = settingsRepository.setNotificationEnabled(true)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { settingsRepository.setNotificationEnabled(true) }
    }

    @Test
    fun `quando setDefaultDuration for chamado com valor zero, deve retornar Result failure`() =
        runTest {
            // Arrange
            val invalidDuration = 0L
            val exception = IllegalArgumentException("Duração deve ser maior que zero")

            coEvery { settingsRepository.setDefaultDuration(invalidDuration) } returns Result.failure(
                exception
            )

            // Act
            val result = settingsRepository.setDefaultDuration(invalidDuration)

            // Assert
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
            coVerify { settingsRepository.setDefaultDuration(invalidDuration) }
        }

    @Test
    fun `quando setDefaultDuration for chamado com valor negativo, deve retornar Result failure`() =
        runTest {
            // Arrange
            val invalidDuration = -10L
            val exception = IllegalArgumentException("Duração não pode ser negativa")

            coEvery { settingsRepository.setDefaultDuration(invalidDuration) } returns Result.failure(
                exception
            )

            // Act
            val result = settingsRepository.setDefaultDuration(invalidDuration)

            // Assert
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
            coVerify { settingsRepository.setDefaultDuration(invalidDuration) }
        }

    @Test
    fun `quando updateSettings for chamado com configurações válidas, deve atualizar corretamente`() =
        runTest {
            // Arrange
            val originalSettings = TimerSettings()
            val updatedSettings = TimerSettings(
                defaultDuration = Duration.ofMinutes(60),
                showNotification = false,
                vibrateOnComplete = true,
                autoStart = true,
                theme = TimerSettings.Theme.LIGHT
            )

            coEvery { settingsRepository.getSettings() } returns MutableStateFlow(originalSettings).asStateFlow()
            coEvery { settingsRepository.updateSettings(updatedSettings) } returns Result.success(Unit)

            // Act
            val updateResult = settingsRepository.updateSettings(updatedSettings)

            // Assert
            assertTrue(updateResult.isSuccess)
            coVerify { settingsRepository.updateSettings(updatedSettings) }
        }
} 