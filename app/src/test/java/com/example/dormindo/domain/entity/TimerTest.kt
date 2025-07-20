package com.example.dormindo.domain.entity

import org.junit.Assert.*
import org.junit.Test
import java.time.Duration

class TimerTest {

    @Test
    fun `deve criar timer com valores padrão corretos`() {
        // Arrange
        val duration = Duration.ofMinutes(30)

        // Act
        val timer = Timer.create(duration)

        // Assert
        assertNotNull(timer.id)
        assertTrue(timer.id.isNotEmpty())
        assertEquals(duration, timer.duration)
        assertFalse(timer.isActive)
        assertEquals(0L, timer.startTime)
        assertEquals(0L, timer.endTime)
        assertEquals(Duration.ZERO, timer.remainingTime)
    }

    @Test
    fun `deve criar timer com id único baseado no timestamp`() {
        // Arrange
        val duration = Duration.ofMinutes(30)

        // Act
        val timer1 = Timer.create(duration)
        val timer2 = Timer.create(duration)

        // Assert
        assertNotEquals(timer1.id, timer2.id)
        assertTrue(timer1.id.isNotEmpty())
        assertTrue(timer2.id.isNotEmpty())
    }

    @Test
    fun `deve criar timer com diferentes durações`() {
        // Arrange
        val duration1 = Duration.ofMinutes(15)
        val duration2 = Duration.ofHours(1)
        val duration3 = Duration.ofSeconds(30)

        // Act
        val timer1 = Timer.create(duration1)
        val timer2 = Timer.create(duration2)
        val timer3 = Timer.create(duration3)

        // Assert
        assertEquals(duration1, timer1.duration)
        assertEquals(duration2, timer2.duration)
        assertEquals(duration3, timer3.duration)
    }

    @Test
    fun `deve criar timer com valores customizados`() {
        // Arrange
        val id = "custom-id"
        val duration = Duration.ofMinutes(45)
        val isActive = true
        val startTime = 1234567890L
        val endTime = 1234567890L + 45 * 60 * 1000L
        val remainingTime = Duration.ofMinutes(30)

        // Act
        val timer = Timer(
            id = id,
            duration = duration,
            isActive = isActive,
            startTime = startTime,
            endTime = endTime,
            remainingTime = remainingTime
        )

        // Assert
        assertEquals(id, timer.id)
        assertEquals(duration, timer.duration)
        assertEquals(isActive, timer.isActive)
        assertEquals(startTime, timer.startTime)
        assertEquals(endTime, timer.endTime)
        assertEquals(remainingTime, timer.remainingTime)
    }

    @Test
    fun `deve criar timer com valores padrão quando parâmetros não fornecidos`() {
        // Arrange
        val duration = Duration.ofMinutes(30)

        // Act
        val timer = Timer(duration = duration)

        // Assert
        assertEquals("", timer.id)
        assertEquals(duration, timer.duration)
        assertFalse(timer.isActive)
        assertEquals(0L, timer.startTime)
        assertEquals(0L, timer.endTime)
        assertEquals(Duration.ZERO, timer.remainingTime)
    }

    @Test
    fun `deve ser igual quando timers tiverem mesmos valores`() {
        // Arrange
        val timer1 = Timer(
            id = "test-id",
            duration = Duration.ofMinutes(30),
            isActive = true,
            startTime = 1000L,
            endTime = 2000L,
            remainingTime = Duration.ofMinutes(15)
        )
        val timer2 = Timer(
            id = "test-id",
            duration = Duration.ofMinutes(30),
            isActive = true,
            startTime = 1000L,
            endTime = 2000L,
            remainingTime = Duration.ofMinutes(15)
        )

        // Act & Assert
        assertEquals(timer1, timer2)
        assertEquals(timer1.hashCode(), timer2.hashCode())
    }

    @Test
    fun `deve ser diferente quando timers tiverem valores diferentes`() {
        // Arrange
        val timer1 = Timer(
            id = "test-id-1",
            duration = Duration.ofMinutes(30),
            isActive = true
        )
        val timer2 = Timer(
            id = "test-id-2",
            duration = Duration.ofMinutes(30),
            isActive = true
        )

        // Act & Assert
        assertNotEquals(timer1, timer2)
        assertNotEquals(timer1.hashCode(), timer2.hashCode())
    }
} 