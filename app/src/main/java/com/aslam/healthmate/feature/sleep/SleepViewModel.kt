package com.aslam.healthmate.feature.sleep

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aslam.healthmate.data.db.AppDatabase
import com.aslam.healthmate.data.db.SleepLog
import com.aslam.healthmate.data.repository.SleepRepository
import com.aslam.healthmate.util.DateUtils
import com.aslam.healthmate.util.SleepCalc
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SleepViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SleepRepository(AppDatabase.getDatabase(app).sleepLogDao())
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    val logs: StateFlow<List<SleepLog>> = repo.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Average minutes per night over the last 7 days (0 if no logs)
    val weeklyAverage: StateFlow<Int> = repo.allLogs
        .map { list ->
            val weekStart = DateUtils.toText(DateUtils.today().minusDays(6))
            val lastWeek = list.filter { it.date >= weekStart }
            if (lastWeek.isEmpty()) 0 else lastWeek.sumOf { it.minutes } / lastWeek.size
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Form
    var date by mutableStateOf(DateUtils.today())
    var bedTime by mutableStateOf(LocalTime.of(22, 30))
    var wakeTime by mutableStateOf(LocalTime.of(6, 30))
    var error by mutableStateOf<String?>(null)
        private set
    var message by mutableStateOf<String?>(null)

    fun preview(): String = SleepCalc.format(SleepCalc.minutesSlept(bedTime, wakeTime))

    fun formatTime(time: LocalTime): String = time.format(timeFormat)

    fun save() {
        viewModelScope.launch {
            error = SleepCalc.check(bedTime, wakeTime)
            if (error == null && repo.getByDate(DateUtils.toText(date)) != null) {
                error = "Sleep for this date is already logged. Delete it first to change it."
            }
            if (error != null) return@launch

            repo.add(
                SleepLog(
                    date = DateUtils.toText(date),
                    bedTime = formatTime(bedTime),
                    wakeTime = formatTime(wakeTime),
                    minutes = SleepCalc.minutesSlept(bedTime, wakeTime)
                )
            )
            message = "Sleep saved"
        }
    }

    fun delete(log: SleepLog) {
        viewModelScope.launch { repo.delete(log) }
    }
}
