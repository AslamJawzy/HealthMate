package com.aslam.healthmate.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// The SQLite database file is called "healthmate.db" and lives inside the app's private storage.
@Database(
    entities = [HealthRecord::class, Food::class, FoodLog::class, SleepLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun foodDao(): FoodDao
    abstract fun foodLogDao(): FoodLogDao
    abstract fun sleepLogDao(): SleepLogDao

    companion object {
        // Only one database object for the whole app (singleton)
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "healthmate.db"
                )
                    // while developing, a schema change just resets the data
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = db
                db
            }
        }
    }
}
