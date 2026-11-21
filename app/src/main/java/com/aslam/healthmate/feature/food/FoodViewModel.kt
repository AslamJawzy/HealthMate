package com.aslam.healthmate.feature.food

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aslam.healthmate.data.db.AppDatabase
import com.aslam.healthmate.data.db.FoodLog
import com.aslam.healthmate.data.repository.FoodRepository
import com.aslam.healthmate.data.repository.HealthRepository
import com.aslam.healthmate.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

// Food diary for one day: what was eaten, and calories eaten vs burned
@OptIn(ExperimentalCoroutinesApi::class)
class FoodViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getDatabase(app)
    private val foodRepo = FoodRepository(db.foodDao(), db.foodLogDao())
    private val healthRepo = HealthRepository(db.healthRecordDao())

    private val _date = MutableStateFlow(DateUtils.today())
    val date: StateFlow<LocalDate> = _date

    val logs: StateFlow<List<FoodLog>> = _date
        .flatMapLatest { foodRepo.logsFor(DateUtils.toText(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calories burned comes from that day's health record
    val burned: StateFlow<Int> = _date
        .flatMapLatest { d -> healthRepo.observeByDate(DateUtils.toText(d)).map { it?.calories ?: 0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun previousDay() {
        _date.value = _date.value.minusDays(1)
    }

    fun nextDay() {
        if (_date.value.isBefore(DateUtils.today())) _date.value = _date.value.plusDays(1)
    }

    fun deleteLog(log: FoodLog) {
        viewModelScope.launch { foodRepo.deleteLog(log) }
    }
}
