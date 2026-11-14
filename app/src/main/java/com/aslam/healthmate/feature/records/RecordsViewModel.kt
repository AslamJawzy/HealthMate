package com.aslam.healthmate.feature.records

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aslam.healthmate.data.db.AppDatabase
import com.aslam.healthmate.data.db.HealthRecord
import com.aslam.healthmate.data.repository.HealthRepository
import com.aslam.healthmate.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

// from and to are the same date when searching for one day
data class DateFilter(val from: LocalDate, val to: LocalDate)

class RecordsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = HealthRepository(AppDatabase.getDatabase(app).healthRecordDao())

    // null = show all records
    private val _filter = MutableStateFlow<DateFilter?>(null)
    val filter: StateFlow<DateFilter?> = _filter

    // Whenever the filter changes, switch to the matching database query
    @OptIn(ExperimentalCoroutinesApi::class)
    val records: StateFlow<List<HealthRecord>> = _filter
        .flatMapLatest { f ->
            if (f == null) repo.allRecords
            else repo.recordsBetween(DateUtils.toText(f.from), DateUtils.toText(f.to))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun searchDate(date: LocalDate) {
        _filter.value = DateFilter(date, date)
    }

    fun searchRange(from: LocalDate, to: LocalDate) {
        // swap if the user picked them the wrong way round
        _filter.value = if (from.isAfter(to)) DateFilter(to, from) else DateFilter(from, to)
    }

    fun clearSearch() {
        _filter.value = null
    }

    // Kept so the user can press Undo
    private var lastDeleted: HealthRecord? = null

    fun delete(record: HealthRecord) {
        viewModelScope.launch {
            repo.delete(record)
            lastDeleted = record
        }
    }

    fun undoDelete() {
        val record = lastDeleted ?: return
        viewModelScope.launch {
            repo.add(record) // same id, so it goes back exactly as it was
            lastDeleted = null
        }
    }
}
