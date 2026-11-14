package com.aslam.healthmate.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Query("SELECT * FROM foods ORDER BY category, name")
    fun getAll(): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): Food?

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun count(): Int

    @Insert
    suspend fun insert(food: Food)

    @Insert
    suspend fun insertAll(foods: List<Food>)

    @Update
    suspend fun update(food: Food)

    @Delete
    suspend fun delete(food: Food)

    @Query("UPDATE foods SET isFavourite = :favourite WHERE id = :id")
    suspend fun setFavourite(id: Int, favourite: Boolean)
}
