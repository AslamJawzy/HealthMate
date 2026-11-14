package com.aslam.healthmate.data.repository

import com.aslam.healthmate.data.db.HealthRecord
import com.aslam.healthmate.data.db.HealthRecordDao
import kotlinx.coroutines.flow.Flow

// The repository is the only place ViewModels get health data from.
// ViewModels don't need to know if the data comes from SQLite or somewhere else.
class HealthRepository(private val dao: HealthRecordDao) {

    val allRecords: Flow<List<HealthRecord>> = dao.getAll()

    fun recordsBetween(from: String, to: String): Flow<List<HealthRecord>> =
        dao.getBetween(from, to)

    suspend fun getById(id: Int): HealthRecord? = dao.getById(id)

    suspend fun getByDate(date: String): HealthRecord? = dao.getByDate(date)

    fun observeByDate(date: String): Flow<HealthRecord?> = dao.observeByDate(date)

    suspend fun add(record: HealthRecord) {
        dao.insert(record)
    }

    suspend fun update(record: HealthRecord) {
        dao.update(record)
    }

    suspend fun delete(record: HealthRecord) {
        dao.delete(record)
    }

    // Used by the quick water buttons. Creates today's record if it doesn't exist yet.
    suspend fun addWater(date: String, ml: Int) {
        val record = dao.getByDate(date)
        if (record == null) {
            dao.insert(HealthRecord(date = date, steps = 0, calories = 0, water = ml))
        } else {
            dao.update(record.copy(water = record.water + ml))
        }
    }

    // Used by the step sensor. Never lowers a value the user typed in by hand.
    suspend fun saveSensorSteps(date: String, steps: Int, calories: Int) {
        val record = dao.getByDate(date)
        if (record == null) {
            dao.insert(HealthRecord(date = date, steps = steps, calories = calories, water = 0))
        } else if (steps > record.steps || calories > record.calories) {
            dao.update(
                record.copy(
                    steps = maxOf(record.steps, steps),
                    calories = maxOf(record.calories, calories)
                )
            )
        }
    }

    suspend fun addSampleDataIfEmpty(samples: List<HealthRecord>) {
        if (dao.count() == 0) {
            dao.insertAll(samples)
        }
    }
}
