package com.aslam.healthmate.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// One night of sleep. date = the morning the user woke up.
@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,      // yyyy-MM-dd
    val bedTime: String,   // HH:mm
    val wakeTime: String,  // HH:mm
    val minutes: Int       // total time asleep
)
