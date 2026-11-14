package com.aslam.healthmate.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// One food eaten on one day. The food name and calories are copied in,
// so the diary still makes sense even if a custom food is deleted later.
@Entity(tableName = "food_logs")
data class FoodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,          // yyyy-MM-dd
    val meal: String,          // Breakfast, Lunch, Dinner, Snack
    val foodName: String,
    val portions: Double,      // 0.5, 1, 1.5, 2
    val calories: Int          // total kcal eaten (food calories x portions)
)
