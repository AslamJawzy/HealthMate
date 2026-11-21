package com.aslam.healthmate.data.prefs

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Small settings are saved in SharedPreferences (a simple key-value file).
// Values the screens need to watch are also kept in StateFlows,
// so the UI updates as soon as a setting changes.
class SettingsPrefs private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("healthmate_settings", Context.MODE_PRIVATE)

    // Daily goals
    private val _stepGoal = MutableStateFlow(prefs.getInt("step_goal", 8000))
    val stepGoal: StateFlow<Int> = _stepGoal

    private val _calorieGoal = MutableStateFlow(prefs.getInt("calorie_goal", 400))
    val calorieGoal: StateFlow<Int> = _calorieGoal

    private val _waterGoal = MutableStateFlow(prefs.getInt("water_goal", 2500))
    val waterGoal: StateFlow<Int> = _waterGoal

    // Profile (used for the calories-from-steps formula)
    private val _heightCm = MutableStateFlow(prefs.getInt("height_cm", 170))
    val heightCm: StateFlow<Int> = _heightCm

    private val _weightKg = MutableStateFlow(prefs.getInt("weight_kg", 70))
    val weightKg: StateFlow<Int> = _weightKg

    private val _isMale = MutableStateFlow(prefs.getBoolean("is_male", true))
    val isMale: StateFlow<Boolean> = _isMale

    fun saveGoals(steps: Int, calories: Int, water: Int) {
        prefs.edit()
            .putInt("step_goal", steps)
            .putInt("calorie_goal", calories)
            .putInt("water_goal", water)
            .apply()
        _stepGoal.value = steps
        _calorieGoal.value = calories
        _waterGoal.value = water
    }

    fun saveProfile(height: Int, weight: Int, male: Boolean) {
        prefs.edit()
            .putInt("height_cm", height)
            .putInt("weight_kg", weight)
            .putBoolean("is_male", male)
            .apply()
        _heightCm.value = height
        _weightKg.value = weight
        _isMale.value = male
    }

    // Set once the sample data has been added, so it is never added twice
    var dummyDataAdded: Boolean
        get() = prefs.getBoolean("dummy_added", false)
        set(value) = prefs.edit().putBoolean("dummy_added", value).apply()

    companion object {
        @Volatile
        private var INSTANCE: SettingsPrefs? = null

        fun get(context: Context): SettingsPrefs {
            return INSTANCE ?: synchronized(this) {
                val p = SettingsPrefs(context.applicationContext)
                INSTANCE = p
                p
            }
        }
    }
}

