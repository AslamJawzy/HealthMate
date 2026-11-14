package com.aslam.healthmate.util

import java.time.Duration
import java.time.LocalTime

object SleepCalc {

    // Minutes between going to bed and waking up.
    // If wake time is "earlier" than bedtime (22:30 -> 06:30) the user slept past midnight.
    fun minutesSlept(bedTime: LocalTime, wakeTime: LocalTime): Int {
        var minutes = Duration.between(bedTime, wakeTime).toMinutes().toInt()
        if (minutes <= 0) minutes += 24 * 60
        return minutes
    }

    // 450 -> "7h 30m"
    fun format(minutes: Int): String = "${minutes / 60}h ${minutes % 60}m"

    // Returns an error message, or null if the times look OK
    fun check(bedTime: LocalTime, wakeTime: LocalTime): String? {
        if (bedTime == wakeTime) return "Bedtime and wake time cannot be the same"
        if (minutesSlept(bedTime, wakeTime) > 16 * 60) return "More than 16 hours of sleep. Please check the times."
        return null
    }
}
