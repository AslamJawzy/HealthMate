package com.aslam.healthmate.feature.records

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aslam.healthmate.data.db.AppDatabase
import com.aslam.healthmate.data.db.HealthRecord
import com.aslam.healthmate.data.prefs.SettingsPrefs
import com.aslam.healthmate.data.repository.HealthRepository
import com.aslam.healthmate.util.CalorieCalc
import com.aslam.healthmate.util.DateUtils
import com.aslam.healthmate.util.RecordErrors
import com.aslam.healthmate.util.Validator
import kotlinx.coroutines.launch

// Used for both "Add record" and "Edit record"
class RecordFormViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = HealthRepository(AppDatabase.getDatabase(app).healthRecordDao())
    private val settings = SettingsPrefs.get(app)

    // Form fields are kept as text, so we can show errors for things like "abc"
    var date by mutableStateOf(DateUtils.toText(DateUtils.today()))
    var steps by mutableStateOf("")
    var calories by mutableStateOf("")
    var water by mutableStateOf("")

    var errors by mutableStateOf(RecordErrors())
        private set

    // Becomes true after a successful save, so the screen can close
    var saved by mutableStateOf(false)
        private set

    // null = adding a new record
    var editingId: Int? = null
        private set

    private var loaded = false

    fun load(id: Int) {
        if (loaded) return // don't reload after screen rotation
        loaded = true
        if (id <= 0) return
        viewModelScope.launch {
            val record = repo.getById(id) ?: return@launch
            editingId = record.id
            date = record.date
            steps = record.steps.toString()
            calories = record.calories.toString()
            water = record.water.toString()
        }
    }

    // Fills the calories box using the steps and the profile in Settings
    fun estimateCalories() {
        val stepCount = steps.trim().toIntOrNull() ?: return
        calories = CalorieCalc.caloriesFromSteps(
            stepCount,
            settings.heightCm.value,
            settings.weightKg.value,
            settings.isMale.value
        ).toString()
    }

    fun save() {
        viewModelScope.launch {
            // Duplicate check: another record already uses this date
            val sameDate = repo.getByDate(date.trim())
            val dateUsed = sameDate != null && sameDate.id != editingId

            val result = Validator.validateRecord(
                date, steps, calories, water, DateUtils.today(), dateUsed
            )
            errors = result
            if (result.hasErrors) return@launch

            val record = HealthRecord(
                id = editingId ?: 0,
                date = date.trim(),
                steps = steps.trim().toInt(),
                calories = calories.trim().toInt(),
                water = water.trim().toInt()
            )
            if (editingId == null) repo.add(record) else repo.update(record)
            saved = true
        }
    }
}
