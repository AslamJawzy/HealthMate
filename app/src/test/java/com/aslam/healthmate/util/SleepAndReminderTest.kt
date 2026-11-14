package com.aslam.healthmate.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class SleepAndReminderTest {

    @Test
    fun sleepPastMidnight_isCountedCorrectly() {
        assertEquals(480, SleepCalc.minutesSlept(LocalTime.of(22, 30), LocalTime.of(6, 30)))
    }

    @Test
    fun sleepAfterMidnight_isCountedCorrectly() {
        assertEquals(410, SleepCalc.minutesSlept(LocalTime.of(0, 10), LocalTime.of(7, 0)))
    }

    @Test
    fun sleepFormat() {
        assertEquals("7h 30m", SleepCalc.format(450))
    }

    @Test
    fun sameBedAndWakeTime_isAnError() {
        assertNotNull(SleepCalc.check(LocalTime.of(22, 0), LocalTime.of(22, 0)))
        assertNull(SleepCalc.check(LocalTime.of(22, 0), LocalTime.of(6, 0)))
    }

    @Test
    fun waterReminders_every90Minutes() {
        val times = ReminderTimes.water
        assertEquals(LocalTime.of(7, 0), times.first())
        assertEquals(LocalTime.of(8, 30), times[1])
        assertEquals(LocalTime.of(22, 0), times.last())
        assertEquals(11, times.size)
    }

    @Test
    fun nextBreathingReminder_laterToday() {
        val now = LocalDateTime.of(2026, 11, 8, 10, 0)
        assertEquals(LocalDateTime.of(2026, 11, 8, 15, 0), ReminderTimes.next(now, ReminderTimes.breathing))
    }

    @Test
    fun nextBreathingReminder_tomorrowMorning() {
        val now = LocalDateTime.of(2026, 11, 8, 21, 30)
        assertEquals(LocalDateTime.of(2026, 11, 9, 7, 0), ReminderTimes.next(now, ReminderTimes.breathing))
    }
}
