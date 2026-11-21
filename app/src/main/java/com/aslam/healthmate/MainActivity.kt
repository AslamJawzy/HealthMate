package com.aslam.healthmate

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.aslam.healthmate.data.db.AppDatabase
import com.aslam.healthmate.data.db.DummyData
import com.aslam.healthmate.data.db.SriLankanFoods
import com.aslam.healthmate.data.prefs.SettingsPrefs
import com.aslam.healthmate.data.repository.FoodRepository
import com.aslam.healthmate.data.repository.HealthRepository
import com.aslam.healthmate.ui.theme.HealthMateTheme
import com.aslam.healthmate.util.DateUtils
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var settings: SettingsPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light theme only, so the status bar icons are always dark
        val style = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        settings = SettingsPrefs.get(this)

        addSampleDataOnFirstLaunch()

        setContent {
            HealthMateTheme {
                HealthMateApp()
            }
        }
    }

    // Adds sample records, the food list and sample food logs the very first time the app is opened
    private fun addSampleDataOnFirstLaunch() {
        if (settings.dummyDataAdded) return
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@MainActivity)
            val today = DateUtils.today()
            HealthRepository(db.healthRecordDao())
                .addSampleDataIfEmpty(DummyData.healthRecords(today))

            val foodRepo = FoodRepository(db.foodDao(), db.foodLogDao())
            foodRepo.addBuiltInFoodsIfEmpty(SriLankanFoods.list())
            foodRepo.addSampleLogsIfEmpty(DummyData.foodLogs(today))

            settings.dummyDataAdded = true
        }
    }
}
