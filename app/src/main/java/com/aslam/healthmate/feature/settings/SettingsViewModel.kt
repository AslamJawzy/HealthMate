package com.aslam.healthmate.feature.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.aslam.healthmate.data.prefs.SettingsPrefs
import com.aslam.healthmate.util.Validator

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val settings = SettingsPrefs.get(app)

    val darkMode = settings.darkMode

    // Goals (as text for the input boxes)
    var stepGoal by mutableStateOf(settings.stepGoal.value.toString())
    var calorieGoal by mutableStateOf(settings.calorieGoal.value.toString())
    var waterGoal by mutableStateOf(settings.waterGoal.value.toString())
    var goalError by mutableStateOf<String?>(null)
        private set

    // Profile
    var height by mutableStateOf(settings.heightCm.value.toString())
    var weight by mutableStateOf(settings.weightKg.value.toString())
    var isMale by mutableStateOf(settings.isMale.value)
    var profileError by mutableStateOf<String?>(null)
        private set

    // Short message shown after saving
    var message by mutableStateOf<String?>(null)

    fun saveGoals() {
        goalError = Validator.checkNumber(stepGoal, "Step goal", Validator.MAX_STEPS)
            ?: Validator.checkNumber(calorieGoal, "Calorie goal", Validator.MAX_CALORIES)
            ?: Validator.checkNumber(waterGoal, "Water goal", Validator.MAX_WATER)
        if (goalError != null) return

        val s = stepGoal.trim().toInt()
        val c = calorieGoal.trim().toInt()
        val w = waterGoal.trim().toInt()
        if (s == 0 || c == 0 || w == 0) {
            goalError = "Goals must be more than 0"
            return
        }
        settings.saveGoals(s, c, w)
        message = "Goals saved"
    }

    fun saveProfile() {
        val h = height.trim().toIntOrNull()
        val w = weight.trim().toIntOrNull()
        profileError = when {
            h == null || h !in 100..250 -> "Height must be between 100 and 250 cm"
            w == null || w !in 20..300 -> "Weight must be between 20 and 300 kg"
            else -> null
        }
        if (profileError != null || h == null || w == null) return
        settings.saveProfile(h, w, isMale)
        message = "Profile saved"
    }

    fun setDarkMode(on: Boolean) {
        settings.setDarkMode(on)
    }
}
