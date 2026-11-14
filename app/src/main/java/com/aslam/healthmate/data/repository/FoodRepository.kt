package com.aslam.healthmate.data.repository

import com.aslam.healthmate.data.db.Food
import com.aslam.healthmate.data.db.FoodDao
import com.aslam.healthmate.data.db.FoodLog
import com.aslam.healthmate.data.db.FoodLogDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FoodRepository(
    private val foodDao: FoodDao,
    private val logDao: FoodLogDao
) {
    // Food list
    val allFoods: Flow<List<Food>> = foodDao.getAll()

    suspend fun findByName(name: String): Food? = foodDao.findByName(name.trim())

    suspend fun addFood(food: Food) = foodDao.insert(food)

    suspend fun updateFood(food: Food) = foodDao.update(food)

    suspend fun deleteFood(food: Food) = foodDao.delete(food)

    suspend fun setFavourite(food: Food, favourite: Boolean) =
        foodDao.setFavourite(food.id, favourite)

    suspend fun addBuiltInFoodsIfEmpty(foods: List<Food>) {
        if (foodDao.count() == 0) foodDao.insertAll(foods)
    }

    // Food diary
    fun logsFor(date: String): Flow<List<FoodLog>> = logDao.getForDate(date)

    fun caloriesEaten(date: String): Flow<Int> = logDao.totalForDate(date).map { it ?: 0 }

    suspend fun addLog(log: FoodLog) = logDao.insert(log)

    suspend fun deleteLog(log: FoodLog) = logDao.delete(log)

    suspend fun addSampleLogsIfEmpty(logs: List<FoodLog>) {
        if (logDao.count() == 0) logDao.insertAll(logs)
    }
}
