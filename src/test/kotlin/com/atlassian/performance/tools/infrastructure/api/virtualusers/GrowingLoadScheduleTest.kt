package com.atlassian.performance.tools.infrastructure.api.virtualusers

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class GrowingLoadScheduleTest {

    @Test(expected = Exception::class)
    fun cantHaveInitialNodesHigherThanFinal() {
        GrowingLoadSchedule(
            duration = Duration.ofMinutes(5),
            finalNodes = 4,
            initialNodes = 6
        )
    }

    @Test
    fun withFinal8AndInitial6OnlyLast2HaveDelay() {
        val schedule = GrowingLoadSchedule(
            duration = Duration.ofMinutes(30),
            finalNodes = 8,
            initialNodes = 6
        )

        assertEquals(3L, schedule.stepCount)
        assertEquals(Duration.ofMinutes(10), schedule.step)

        assertEquals(Duration.ZERO, schedule.startingDelay(1))
        assertEquals(Duration.ZERO, schedule.startingDelay(2))
        assertEquals(Duration.ZERO, schedule.startingDelay(3))
        assertEquals(Duration.ZERO, schedule.startingDelay(4))
        assertEquals(Duration.ZERO, schedule.startingDelay(5))
        assertEquals(Duration.ZERO, schedule.startingDelay(6))
        assertEquals(Duration.ofMinutes(10), schedule.startingDelay(7))
        assertEquals(Duration.ofMinutes(20), schedule.startingDelay(8))
    }

    @Test
    fun noDelayWhenInitialNodesEqualsFinal() {
        val schedule = GrowingLoadSchedule(
            duration = Duration.ofHours(24),
            finalNodes = 96,
            initialNodes = 96
        )

        assertEquals(1L, schedule.stepCount)
        assertEquals(Duration.ofHours(24), schedule.step)

        (1..96).forEach {
            assertEquals(Duration.ZERO, schedule.startingDelay(it))
        }
    }

    @Test
    fun whenStartingWithoutNodesEverythingStartsWithDelay() {
        val schedule = GrowingLoadSchedule(
            duration = Duration.ofMinutes(30),
            finalNodes = 5,
            initialNodes = 0
        )

        assertEquals(6L, schedule.stepCount)
        assertEquals(Duration.ofMinutes(5), schedule.step)

        assertEquals(Duration.ofMinutes(5), schedule.startingDelay(1))
        assertEquals(Duration.ofMinutes(10), schedule.startingDelay(2))
        assertEquals(Duration.ofMinutes(15), schedule.startingDelay(3))
        assertEquals(Duration.ofMinutes(20), schedule.startingDelay(4))
        assertEquals(Duration.ofMinutes(25), schedule.startingDelay(5))
    }

    @Test
    fun whenStartingWith1NodeOnlyFirstHasNoDelay() {
        val schedule = GrowingLoadSchedule(
            duration = Duration.ofMinutes(15),
            finalNodes = 5,
            initialNodes = 1
        )

        assertEquals(5L, schedule.stepCount)
        assertEquals(Duration.ofMinutes(3), schedule.step)

        assertEquals(Duration.ZERO, schedule.startingDelay(1))
        assertEquals(Duration.ofMinutes(3), schedule.startingDelay(2))
        assertEquals(Duration.ofMinutes(6), schedule.startingDelay(3))
        assertEquals(Duration.ofMinutes(9), schedule.startingDelay(4))
        assertEquals(Duration.ofMinutes(12), schedule.startingDelay(5))
    }
}
