package com.aslam.healthmate.data.db

import com.aslam.healthmate.util.DateUtils
import java.time.LocalDate

// Sample records so the app has something to show the first time it opens
object DummyData {

    // 14 days ending today. Values are fixed so the demo always looks the same.
    fun healthRecords(today: LocalDate): List<HealthRecord> {
        val steps = listOf(6420, 8150, 9870, 5230, 7600, 11200, 4300, 8900, 7350, 10120, 6780, 9450, 8020, 7850)
        val calories = listOf(310, 395, 470, 260, 365, 540, 210, 430, 350, 490, 330, 455, 385, 420)
        val water = listOf(1800, 2250, 2500, 1500, 2000, 2750, 1250, 2300, 2000, 2600, 1750, 2400, 2200, 2000)

        val list = mutableListOf<HealthRecord>()
        for (i in 0 until 14) {
            val date = today.minusDays((13 - i).toLong())
            list.add(
                HealthRecord(
                    date = DateUtils.toText(date),
                    steps = steps[i],
                    calories = calories[i],
                    water = water[i]
                )
            )
        }
        return list
    }

    // A few meals for today and yesterday so the food diary isn't empty
    fun foodLogs(today: LocalDate): List<FoodLog> {
        val t = DateUtils.toText(today)
        val y = DateUtils.toText(today.minusDays(1))
        return listOf(
            FoodLog(date = y, meal = "Breakfast", foodName = "String hoppers", portions = 2.0, calories = 350),
            FoodLog(date = y, meal = "Breakfast", foodName = "Dhal curry (parippu)", portions = 1.0, calories = 180),
            FoodLog(date = y, meal = "Lunch", foodName = "Rice and curry (chicken)", portions = 1.0, calories = 750),
            FoodLog(date = y, meal = "Dinner", foodName = "Chicken kottu", portions = 1.0, calories = 800),
            FoodLog(date = t, meal = "Breakfast", foodName = "Kiribath", portions = 1.0, calories = 300),
            FoodLog(date = t, meal = "Breakfast", foodName = "Lunu miris", portions = 1.0, calories = 30),
            FoodLog(date = t, meal = "Breakfast", foodName = "Milk tea", portions = 1.0, calories = 90)
        )
    }

    // Last 7 nights of sleep
    fun sleepLogs(today: LocalDate): List<SleepLog> {
        val times = listOf(
            Triple("22:45", "06:15", 450), Triple("23:30", "06:30", 420), Triple("22:15", "06:00", 465),
            Triple("00:10", "07:00", 410), Triple("23:00", "06:45", 465), Triple("22:30", "05:45", 435),
            Triple("23:15", "06:30", 435)
        )
        return times.mapIndexed { i, (bed, wake, minutes) ->
            SleepLog(
                date = DateUtils.toText(today.minusDays((6 - i).toLong())),
                bedTime = bed,
                wakeTime = wake,
                minutes = minutes
            )
        }
    }
}
