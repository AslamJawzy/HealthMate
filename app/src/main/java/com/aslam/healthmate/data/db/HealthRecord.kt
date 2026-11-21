package com.aslam.healthmate.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// One row in the health_records table = one day of activity.
// Room turns this class into:
// CREATE TABLE health_records (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//   date TEXT NOT NULL, steps INTEGER NOT NULL, calories INTEGER NOT NULL, water INTEGER NOT NULL)
@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "date") val date: String,      // yyyy-MM-dd
    @ColumnInfo(name = "steps") val steps: Int,
    @ColumnInfo(name = "calories") val calories: Int, // calories burned
    @ColumnInfo(name = "water") val water: Int        // ml
)
