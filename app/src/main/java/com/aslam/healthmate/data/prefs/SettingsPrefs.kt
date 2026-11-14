package com.aslam.healthmate.data.prefs

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.aslam.healthmate.util.StepState

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

    // Theme
    private val _darkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val darkMode: StateFlow<Boolean> = _darkMode

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

    fun setDarkMode(on: Boolean) {
        prefs.edit().putBoolean("dark_mode", on).apply()
        _darkMode.value = on
    }

    // Set once the sample data has been added, so it is never added twice
    var dummyDataAdded: Boolean
        get() = prefs.getBoolean("dummy_added", false)
        set(value) = prefs.edit().putBoolean("dummy_added", value).apply()

    // Reminder switches (key is the reminder type, like "breathing")
    fun isReminderOn(type: String): Boolean = prefs.getBoolean("reminder_$type", false)

    fun setReminderOn(type: String, on: Boolean) {
        prefs.edit().putBoolean("reminder_$type", on).apply()
    }

    // Saved state for the step counter (see StepCounterLogic)
    fun loadStepState(): StepState {
        return StepState(
            date = prefs.getString("step_date", "") ?: "",
            baseline = prefs.getLong("step_baseline", 0L),
            lastReading = prefs.getLong("step_last", 0L),
            offset = prefs.getLong("step_offset", 0L)
        )
    }

    fun saveStepState(state: StepState) {
        prefs.edit()
            .putString("step_date", state.date)
            .putLong("step_baseline", state.baseline)
            .putLong("step_last", state.lastReading)
            .putLong("step_offset", state.offset)
            .apply()
    }

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

