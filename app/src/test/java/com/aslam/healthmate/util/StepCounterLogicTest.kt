package com.aslam.healthmate.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StepCounterLogicTest {

    private val empty = StepState(date = "", baseline = 0, lastReading = 0, offset = 0)

    @Test
    fun firstReadingEver_startsAtZero() {
        val result = StepCounterLogic.onReading(15000, "2026-11-08", empty)
        assertEquals(0, result.stepsToday)
        assertEquals(15000L, result.newState.baseline)
    }

    @Test
    fun normalCounting_addsSteps() {
        val first = StepCounterLogic.onReading(15000, "2026-11-08", empty)
        val second = StepCounterLogic.onReading(15250, "2026-11-08", first.newState)
        assertEquals(250, second.stepsToday)
        val third = StepCounterLogic.onReading(16000, "2026-11-08", second.newState)
        assertEquals(1000, third.stepsToday)
    }

    @Test
    fun phoneRestart_keepsEarlierSteps() {
        // 1000 steps counted, then the phone restarts and the sensor starts again from 0
        val state = StepState("2026-11-08", baseline = 15000, lastReading = 16000, offset = 0)
        val afterRestart = StepCounterLogic.onReading(30, "2026-11-08", state)
        assertEquals(1030, afterRestart.stepsToday)

        // keeps counting normally after the restart
        val later = StepCounterLogic.onReading(500, "2026-11-08", afterRestart.newState)
        assertEquals(1500, later.stepsToday)
    }

    @Test
    fun newDay_resetsToZero() {
        val yesterday = StepState("2026-11-07", baseline = 10000, lastReading = 18000, offset = 0)
        val result = StepCounterLogic.onReading(18100, "2026-11-08", yesterday)
        assertEquals(0, result.stepsToday)
        assertEquals("2026-11-08", result.newState.date)

        val later = StepCounterLogic.onReading(18600, "2026-11-08", result.newState)
        assertEquals(500, later.stepsToday)
    }
}
