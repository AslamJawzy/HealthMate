package com.aslam.healthmate.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepLogDao {

    @Query("SELECT * FROM sleep_logs ORDER BY date DESC")
    fun getAll(): Flow<List<SleepLog>>

    @Query("SELECT * FROM sleep_logs WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): SleepLog?

    @Query("SELECT COUNT(*) FROM sleep_logs")
    suspend fun count(): Int

    @Insert
    suspend fun insert(log: SleepLog)

    @Insert
    suspend fun insertAll(logs: List<SleepLog>)

    @Delete
    suspend fun delete(log: SleepLog)
}
