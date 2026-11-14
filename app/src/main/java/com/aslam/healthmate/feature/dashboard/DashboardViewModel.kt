package com.aslam.healthmate.feature.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aslam.healthmate.data.db.AppDatabase
import com.aslam.healthmate.data.db.HealthRecord
import com.aslam.healthmate.data.prefs.SettingsPrefs
import com.aslam.healthmate.data.repository.FoodRepository
import com.aslam.healthmate.data.repository.HealthRepository
import com.aslam.healthmate.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class Period { DAY, WEEK, MONTH }

enum class ChartMetric { STEPS, CALORIES, WATER }

// One bar in the weekly chart. record is null if nothing was saved that day.
data class ChartDay(val date: LocalDate, val record: HealthRecord?)

// Everything the dashboard needs to show for the chosen day, week or month
data class Summary(
    val label: String = "",
    val days: Int = 0,          // how many days have a record
    val totalSteps: Int = 0,
    val totalCalories: Int = 0,
    val totalWater: Int = 0
) {
    val avgSteps: Int get() = if (days > 0) totalSteps / days else 0
    val avgCalories: Int get() = if (days > 0) totalCalories / days else 0
    val avgWater: Int get() = if (days > 0) totalWater / days else 0
}

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getDatabase(app)
    private val repo = HealthRepository(db.healthRecordDao())
    private val foodRepo = FoodRepository(db.foodDao(), db.foodLogDao())
    private val settings = SettingsPrefs.get(app)

    val stepGoal = settings.stepGoal
    val calorieGoal = settings.calorieGoal
    val waterGoal = settings.waterGoal

    private val _period = MutableStateFlow(Period.DAY)
    val period: StateFlow<Period> = _period

    // Any date inside the chosen day/week/month
    private val _date = MutableStateFlow(DateUtils.today())
    val date: StateFlow<LocalDate> = _date

    @OptIn(ExperimentalCoroutinesApi::class)
    val summary: StateFlow<Summary> = combine(_period, _date) { p, d -> Pair(p, d) }
        .flatMapLatest { (p, d) ->
            val (from, to) = rangeFor(p, d)
            repo.recordsBetween(DateUtils.toText(from), DateUtils.toText(to))
                .map { list -> makeSummary(p, d, list) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Summary())

    // Calories eaten on the chosen day (from the food diary)
    @OptIn(ExperimentalCoroutinesApi::class)
    val caloriesEaten: StateFlow<Int> = _date
        .flatMapLatest { foodRepo.caloriesEaten(DateUtils.toText(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Weekly chart: always the last 7 days up to today
    private val chartStart = DateUtils.today().minusDays(6)

    val last7Days: StateFlow<List<ChartDay>> = repo
        .recordsBetween(DateUtils.toText(chartStart), DateUtils.toText(DateUtils.today()))
        .map { list ->
            (0..6).map { i ->
                val day = chartStart.plusDays(i.toLong())
                ChartDay(day, list.find { it.date == DateUtils.toText(day) })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _chartMetric = MutableStateFlow(ChartMetric.STEPS)
    val chartMetric: StateFlow<ChartMetric> = _chartMetric

    fun setChartMetric(m: ChartMetric) {
        _chartMetric.value = m
    }

    fun setPeriod(p: Period) {
        _period.value = p
        _date.value = DateUtils.today() // jump back to the current day/week/month
    }

    fun previous() {
        _date.value = when (_period.value) {
            Period.DAY -> _date.value.minusDays(1)
            Period.WEEK -> _date.value.minusWeeks(1)
            Period.MONTH -> _date.value.minusMonths(1)
        }
    }

    fun next() {
        if (!canGoNext()) return
        _date.value = when (_period.value) {
            Period.DAY -> _date.value.plusDays(1)
            Period.WEEK -> _date.value.plusWeeks(1)
            Period.MONTH -> _date.value.plusMonths(1)
        }
    }

    // No going into the future
    fun canGoNext(): Boolean {
        val (_, end) = rangeFor(_period.value, _date.value)
        return end.isBefore(DateUtils.today())
    }

    // Quick add buttons always add to today's record
    fun addWater(ml: Int) {
        viewModelScope.launch {
            repo.addWater(DateUtils.toText(DateUtils.today()), ml)
        }
    }

    private fun rangeFor(p: Period, d: LocalDate): Pair<LocalDate, LocalDate> {
        return when (p) {
            Period.DAY -> Pair(d, d)
            Period.WEEK -> DateUtils.weekRange(d)
            Period.MONTH -> DateUtils.monthRange(d)
        }
    }

    private fun makeSummary(p: Period, d: LocalDate, list: List<HealthRecord>): Summary {
        val label = when (p) {
            Period.DAY -> if (d == DateUtils.today()) "Today, ${DateUtils.short(d)}" else DateUtils.pretty(d)
            Period.WEEK -> {
                val (from, to) = DateUtils.weekRange(d)
                "${DateUtils.short(from)} to ${DateUtils.short(to)}"
            }
            Period.MONTH -> d.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        }
        return Summary(
            label = label,
            days = list.size,
            totalSteps = list.sumOf { it.steps },
            totalCalories = list.sumOf { it.calories },
            totalWater = list.sumOf { it.water }
        )
    }
}
