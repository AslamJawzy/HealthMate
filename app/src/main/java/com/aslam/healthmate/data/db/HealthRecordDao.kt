package com.aslam.healthmate.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// DAO = Data Access Object. Each function here becomes a SQL statement.
// Functions that return Flow update the UI automatically when the table changes.
@Dao
interface HealthRecordDao {

    // CREATE
    @Insert
    suspend fun insert(record: HealthRecord): Long

    @Insert
    suspend fun insertAll(records: List<HealthRecord>)

    // READ
    @Query("SELECT * FROM health_records ORDER BY date DESC")
    fun getAll(): Flow<List<HealthRecord>>

    // Dates are saved as yyyy-MM-dd text, so normal text comparison works for ranges
    @Query("SELECT * FROM health_records WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun getBetween(from: String, to: String): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE id = :id")
    suspend fun getById(id: Int): HealthRecord?

    @Query("SELECT * FROM health_records WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): HealthRecord?

    // Same as getByDate but keeps watching for changes
    @Query("SELECT * FROM health_records WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<HealthRecord?>

    @Query("SELECT COUNT(*) FROM health_records")
    suspend fun count(): Int

    // UPDATE
    @Update
    suspend fun update(record: HealthRecord)

    // DELETE
    @Delete
    suspend fun delete(record: HealthRecord)
}
