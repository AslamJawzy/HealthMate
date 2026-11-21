package com.aslam.healthmate.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {

    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY id")
    fun getForDate(date: String): Flow<List<FoodLog>>

    // Total calories eaten on one day (null if nothing logged)
    @Query("SELECT SUM(calories) FROM food_logs WHERE date = :date")
    fun totalForDate(date: String): Flow<Int?>

    @Query("SELECT COUNT(*) FROM food_logs")
    suspend fun count(): Int

    @Insert
    suspend fun insert(log: FoodLog)

    @Insert
    suspend fun insertAll(logs: List<FoodLog>)

    @Delete
    suspend fun delete(log: FoodLog)
}
