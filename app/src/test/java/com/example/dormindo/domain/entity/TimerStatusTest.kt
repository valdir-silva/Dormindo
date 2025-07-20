package com.example.dormindo.domain.entity

import org.junit.Assert.*
import org.junit.Test
import java.time.Duration

class TimerStatusTest {

    @Test
    fun `deve criar status idle`() {
        // Act
        val status = TimerStatus.Idle

        // Assert
        assertTrue(status is TimerStatus.Idle)
    }

    @Test
    fun `deve criar status running`() {
        // Act
        val status = TimerStatus.Running

        // Assert
        assertTrue(status is TimerStatus.Running)
    }

    @Test
    fun `deve criar status paused com tempo restante`() {
        // Arrange
        val remainingTime = Duration.ofMinutes(15)

        // Act
        val status = TimerStatus.Paused(remainingTime)

        // Assert
        assertTrue(status is TimerStatus.Paused)
        assertEquals(remainingTime, status.remainingTime)
    }

    @Test
    fun `deve criar status completed`() {
        // Act
        val status = TimerStatus.Completed

        // Assert
        assertTrue(status is TimerStatus.Completed)
    }

    @Test
    fun `deve criar status error com mensagem`() {
        // Arrange
        val errorMessage = "Erro no timer"
        // Act
        val status = TimerStatus.Error(errorMessage)

        // Assert
        assertTrue(status is TimerStatus.Error)
        assertEquals(errorMessage, status.message)
    }

    @Test
    fun `deve ser igual quando status idle for comparado`() {
        // Arrange
        val status1 = TimerStatus.Idle
        val status2 = TimerStatus.Idle

        // Act & Assert
        assertEquals(status1, status2)
        assertEquals(status1.hashCode(), status2.hashCode())
    }

    @Test
    fun `deve ser igual quando status running for comparado`() {
        // Arrange
        val status1 = TimerStatus.Running
        val status2 = TimerStatus.Running

        // Act & Assert
        assertEquals(status1, status2)
        assertEquals(status1.hashCode(), status2.hashCode())
    }

    @Test
    fun `deve ser igual quando status paused tiver mesmo tempo restante`() {
        // Arrange
        val remainingTime = Duration.ofMinutes(30)
        val status1 = TimerStatus.Paused(remainingTime)
        val status2 = TimerStatus.Paused(remainingTime)

        // Act & Assert
        assertEquals(status1, status2)
        assertEquals(status1.hashCode(), status2.hashCode())
    }

    @Test
    fun `deve ser diferente quando status paused tiver tempos diferentes`() {
        // Arrange
        val status1 = TimerStatus.Paused(Duration.ofMinutes(15))
        val status2 = TimerStatus.Paused(Duration.ofMinutes(30))

        // Act & Assert
        assertNotEquals(status1, status2)
        assertNotEquals(status1.hashCode(), status2.hashCode())
    }

    @Test
    fun `deve ser igual quando status completed for comparado`() {
        // Arrange
        val status1 = TimerStatus.Completed
        val status2 = TimerStatus.Completed

        // Act & Assert
        assertEquals(status1, status2)
        assertEquals(status1.hashCode(), status2.hashCode())
    }

    @Test
    fun `deve ser igual quando status error tiver mesma mensagem`() {
        // Arrange
        val errorMessage = "Erro no timer"
        val status1 = TimerStatus.Error(errorMessage)
        val status2 = TimerStatus.Error(errorMessage)

        // Act & Assert
        assertEquals(status1, status2)
        assertEquals(status1.hashCode(), status2.hashCode())
    }

    @Test
    fun `deve ser diferente quando status error tiver mensagens diferentes`() {
        // Arrange
        val status1 = TimerStatus.Error("Erro 1")
        val status2 = TimerStatus.Error("Erro 2")

        // Act & Assert
        assertNotEquals(status1, status2)
        assertNotEquals(status1.hashCode(), status2.hashCode())
    }

    @Test
    fun `deve ser diferente quando status for de tipos diferentes`() {
        // Arrange
        val idleStatus = TimerStatus.Idle
        val runningStatus = TimerStatus.Running
        val pausedStatus = TimerStatus.Paused(Duration.ofMinutes(15))
        val completedStatus = TimerStatus.Completed
        val errorStatus = TimerStatus.Error("Erro")

        // Act & Assert
        assertNotEquals(idleStatus, runningStatus)
        assertNotEquals(idleStatus, pausedStatus)
        assertNotEquals(idleStatus, completedStatus)
        assertNotEquals(idleStatus, errorStatus)
        assertNotEquals(runningStatus, pausedStatus)
        assertNotEquals(runningStatus, completedStatus)
        assertNotEquals(runningStatus, errorStatus)
        assertNotEquals(pausedStatus, completedStatus)
        assertNotEquals(pausedStatus, errorStatus)
        assertNotEquals(completedStatus, errorStatus)
    }
} 