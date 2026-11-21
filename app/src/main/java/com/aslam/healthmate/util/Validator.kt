package com.aslam.healthmate.util

import java.time.LocalDate

// Holds one error message per field. null means the field is OK.
data class RecordErrors(
    val date: String? = null,
    val steps: String? = null,
    val calories: String? = null,
    val water: String? = null
) {
    val hasErrors: Boolean
        get() = date != null || steps != null || calories != null || water != null
}

data class FoodErrors(
    val name: String? = null,
    val calories: String? = null,
    val portion: String? = null
) {
    val hasErrors: Boolean
        get() = name != null || calories != null || portion != null
}

// All the form checks live here so they can be unit tested without a phone
object Validator {

    // Upper limits so silly values like 999999 steps are rejected
    const val MAX_STEPS = 100_000
    const val MAX_CALORIES = 10_000
    const val MAX_WATER = 10_000
    const val MAX_FOOD_CALORIES = 3_000

    fun validateRecord(
        date: String,
        steps: String,
        calories: String,
        water: String,
        today: LocalDate,
        dateAlreadyUsed: Boolean
    ): RecordErrors {
        return RecordErrors(
            date = checkDate(date, today, dateAlreadyUsed),
            steps = checkNumber(steps, "Steps", MAX_STEPS),
            calories = checkNumber(calories, "Calories", MAX_CALORIES),
            water = checkNumber(water, "Water", MAX_WATER)
        )
    }

    fun checkDate(text: String, today: LocalDate, dateAlreadyUsed: Boolean): String? {
        if (text.isBlank()) return "Please choose a date"
        val date = DateUtils.parse(text) ?: return "Date must be in yyyy-MM-dd format"
        if (date.isAfter(today)) return "Date cannot be in the future"
        if (dateAlreadyUsed) return "A record for this date already exists. Edit that one instead."
        return null
    }

    fun checkNumber(text: String, name: String, max: Int): String? {
        if (text.isBlank()) return "Please enter $name"
        val number = text.trim().toIntOrNull() ?: return "$name must be a whole number"
        if (number < 0) return "$name cannot be negative"
        if (number > max) return "$name looks too high (max $max)"
        return null
    }

    fun validateFood(name: String, calories: String, portion: String, nameAlreadyUsed: Boolean): FoodErrors {
        val nameError = when {
            name.isBlank() -> "Please enter a food name"
            nameAlreadyUsed -> "This food is already in the list"
            else -> null
        }
        var caloriesError = checkNumber(calories, "Calories", MAX_FOOD_CALORIES)
        if (caloriesError == null && calories.trim().toInt() == 0) {
            caloriesError = "Calories must be more than 0"
        }
        val portionError = if (portion.isBlank()) "Please enter a portion, like 1 cup" else null
        return FoodErrors(nameError, caloriesError, portionError)
    }
}
