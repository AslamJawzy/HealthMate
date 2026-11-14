package com.aslam.healthmate.feature.steps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.aslam.healthmate.data.db.AppDatabase
import com.aslam.healthmate.data.prefs.SettingsPrefs
import com.aslam.healthmate.data.repository.HealthRepository
import com.aslam.healthmate.ui.components.formatNumber
import com.aslam.healthmate.util.CalorieCalc
import com.aslam.healthmate.util.DateUtils
import com.aslam.healthmate.util.StepCounterLogic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

// Reads the phone's step sensor while the app is open and saves today's steps.
// No background service: the sensor keeps its own total, so steps walked while
// the app was closed are still picked up the next time it opens (same day).
object StepTracker : SensorEventListener {

    private var appContext: Context? = null
    private var sensorManager: SensorManager? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastSavedSteps = -1

    // Text shown on the dashboard
    private val _status = MutableStateFlow("Step counter is starting...")
    val status: StateFlow<String> = _status

    fun hasPermission(context: Context): Boolean {
        // Before Android 10 this permission did not exist
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun start(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (sensor == null) {
            // e.g. the emulator
            _status.value = "No step sensor on this device. Add steps by hand in Records."
            return
        }
        if (!hasPermission(ctx)) {
            _status.value = "Allow the Physical activity permission to count steps automatically."
            return
        }
        if (sensorManager == null) {
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager = sm
            _status.value = "Waiting for the step sensor..."
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        val ctx = appContext ?: return
        val settings = SettingsPrefs.get(ctx)
        val today = DateUtils.toText(DateUtils.today())

        val reading = event.values[0].toLong()
        val result = StepCounterLogic.onReading(reading, today, settings.loadStepState())
        settings.saveStepState(result.newState)

        val steps = result.stepsToday
        _status.value = "Live: ${formatNumber(steps)} steps today"

        // Save every 10 steps, not on every single step
        if (steps > 0 && (lastSavedSteps < 0 || abs(steps - lastSavedSteps) >= 10)) {
            lastSavedSteps = steps
            val kcal = CalorieCalc.caloriesFromSteps(
                steps, settings.heightCm.value, settings.weightKg.value, settings.isMale.value
            )
            val repo = HealthRepository(AppDatabase.getDatabase(ctx).healthRecordDao())
            scope.launch {
                repo.saveSensorSteps(today, steps, kcal)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // not needed
    }
}
