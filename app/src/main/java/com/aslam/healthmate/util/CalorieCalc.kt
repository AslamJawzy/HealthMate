package com.aslam.healthmate.util

import kotlin.math.roundToInt

// Estimates calories burned from steps using the user's profile.
// Example: 8,000 steps, 170 cm, 70 kg, male = about 209 kcal
object CalorieCalc {

    // Stride length is roughly a fixed part of a person's height
    fun strideCm(heightCm: Int, isMale: Boolean): Double {
        val factor = if (isMale) 0.415 else 0.413
        return heightCm * factor
    }

    // 100,000 cm = 1 km
    fun distanceKm(steps: Int, heightCm: Int, isMale: Boolean): Double {
        return steps * strideCm(heightCm, isMale) / 100_000
    }

    fun caloriesFromSteps(steps: Int, heightCm: Int, weightKg: Int, isMale: Boolean): Int {
        val km = distanceKm(steps, heightCm, isMale)
        return (km * weightKg * 0.53).roundToInt()
    }
}
