package com.aslam.healthmate.feature.reminders

import com.aslam.healthmate.util.ReminderTimes
import java.time.LocalTime

// The three kinds of reminder. The name (e.g. "BREATHING") is also the saved settings key.
enum class ReminderType(
    val title: String,
    val message: String,
    val schedule: String,
    val times: List<LocalTime>
) {
    BREATHING(
        "Time to breathe",
        "Take 2 minutes for a box breathing exercise.",
        "7:00 AM, 3:00 PM and 9:00 PM",
        ReminderTimes.breathing
    ),
    WATER(
        "Drink some water",
        "Have a glass of water and log it in HealthMate.",
        "Every 1.5 hours, 7:00 AM to 10:00 PM",
        ReminderTimes.water
    ),
    MOVE(
        "Time to move",
        "Stand up and walk around for a few minutes.",
        "Every hour, 8:00 AM to 9:00 PM",
        ReminderTimes.move
    )
}
