package com.aslam.healthmate.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// A food the user can pick in the food diary.
// Built-in Sri Lankan foods have isCustom = false. Foods the user adds have isCustom = true.
@Entity(tableName = "foods")
data class Food(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val calories: Int,       // kcal for one portion
    val portion: String,     // e.g. "1 plate", "1 cup"
    val isCustom: Boolean = false,
    val isFavourite: Boolean = false
)
