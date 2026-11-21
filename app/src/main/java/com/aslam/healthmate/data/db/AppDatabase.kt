package com.aslam.healthmate.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// The SQLite database file is called "healthmate.db" and lives inside the app's private storage.
@Database(
    entities = [HealthRecord::class, Food::class, FoodLog::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun foodDao(): FoodDao
    abstract fun foodLogDao(): FoodLogDao

    companion object {
        // Only one database object for the whole app (singleton)
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Version 1 also had a sleep_logs table for the sleep tracker, which was removed.
        // This migration only drops that table, so health records and food data are kept.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `sleep_logs`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "healthmate.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    // only used if a schema change has no migration
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = db
                db
            }
        }
    }
}
