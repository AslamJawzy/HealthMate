package com.aslam.healthmate

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aslam.healthmate.data.db.AppDatabase
import com.aslam.healthmate.data.db.DummyData
import com.aslam.healthmate.data.db.SriLankanFoods
import com.aslam.healthmate.data.prefs.SettingsPrefs
import com.aslam.healthmate.data.repository.FoodRepository
import com.aslam.healthmate.data.repository.HealthRepository
import com.aslam.healthmate.data.repository.SleepRepository
import com.aslam.healthmate.feature.reminders.NotificationHelper
import com.aslam.healthmate.feature.reminders.ReminderScheduler
import com.aslam.healthmate.feature.screentime.ScreenTime
import com.aslam.healthmate.feature.screentime.ScreenTimeDialog
import com.aslam.healthmate.feature.steps.StepTracker
import com.aslam.healthmate.ui.theme.HealthMateTheme
import com.aslam.healthmate.util.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var settings: SettingsPrefs

    // Screen to open when the app was started from a notification (e.g. "breathing")
    private var openScreen by mutableStateOf<String?>(null)

    // Screen time pop-up
    private var screenTimeMessage by mutableStateOf<String?>(null)

    // Asks for the step permission. If the user says yes, start counting.
    private val stepPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) StepTracker.start(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        settings = SettingsPrefs.get(this)
        openScreen = intent.getStringExtra(NotificationHelper.EXTRA_OPEN_SCREEN)

        addSampleDataOnFirstLaunch()
        askStepPermission()
        NotificationHelper.createChannel(this)
        ReminderScheduler.rescheduleAll(this)
        startScreenTimeTimer()

        setContent {
            val darkMode by settings.darkMode.collectAsState()

            // Status bar icons: light icons in dark mode, dark icons in light mode
            LaunchedEffect(darkMode) {
                val style = if (darkMode) SystemBarStyle.dark(Color.TRANSPARENT)
                else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            HealthMateTheme(darkMode = darkMode) {
                HealthMateApp(
                    openScreen = openScreen,
                    onScreenOpened = { openScreen = null }
                )
                screenTimeMessage?.let { message ->
                    ScreenTimeDialog(message, onDismiss = { screenTimeMessage = null })
                }
            }
        }
    }

    // App was already open and the user tapped a notification
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openScreen = intent.getStringExtra(NotificationHelper.EXTRA_OPEN_SCREEN)
    }

    // The step sensor only runs while the app is on screen
    override fun onResume() {
        super.onResume()
        StepTracker.start(this)
    }

    override fun onPause() {
        super.onPause()
        StepTracker.stop()
    }

    // Counts time only while the app is on screen. If the user leaves the app,
    // the coroutine is cancelled, so the count starts again from 0 next time.
    private fun startScreenTimeTimer() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    delay(ScreenTime.LIMIT_MILLIS)
                    screenTimeMessage = ScreenTime.randomMessage()
                }
            }
        }
    }

    private fun askStepPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !StepTracker.hasPermission(this)) {
            stepPermissionRequest.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    // Adds sample records, the food list and sample food/sleep logs the very first time the app is opened
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

            SleepRepository(db.sleepLogDao()).addSampleLogsIfEmpty(DummyData.sleepLogs(today))
            settings.dummyDataAdded = true
        }
    }
}
