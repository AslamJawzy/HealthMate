package com.aslam.healthmate.util

import java.time.LocalDateTime
import java.time.LocalTime

// Times of day for each reminder, and how to find the next one
object ReminderTimes {

    // Before breakfast, 2 hours after a 1 PM lunch, 2 hours after a 7 PM dinner
    val breathing: List<LocalTime> = listOf(LocalTime.of(7, 0), LocalTime.of(15, 0), LocalTime.of(21, 0))

    // Every 1.5 hours while awake (7:00 AM to 10:00 PM)
    val water: List<LocalTime> = everyMinutes(LocalTime.of(7, 0), LocalTime.of(22, 0), 90)

    // Every hour from 8:00 AM until 9:00 PM, before sleep time
    val move: List<LocalTime> = everyMinutes(LocalTime.of(8, 0), LocalTime.of(21, 0), 60)

    fun everyMinutes(start: LocalTime, end: LocalTime, step: Long): List<LocalTime> {
        val list = mutableListOf<LocalTime>()
        var t = start
        while (!t.isAfter(end)) {
            list.add(t)
            t = t.plusMinutes(step)
        }
        return list
    }

    // The first time in the list that is still ahead of "now".
    // If all of today's times have passed, the first time tomorrow.
    fun next(now: LocalDateTime, times: List<LocalTime>): LocalDateTime {
        val sorted = times.sorted()
        for (t in sorted) {
            val candidate = now.toLocalDate().atTime(t)
            if (candidate.isAfter(now)) return candidate
        }
        return now.toLocalDate().plusDays(1).atTime(sorted.first())
    }
}
