package com.aslam.healthmate.feature.reminders

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import com.aslam.healthmate.data.prefs.SettingsPrefs

class RemindersViewModel(app: Application) : AndroidViewModel(app) {

    private val settings = SettingsPrefs.get(app)
    private val context = app.applicationContext

    // On/off state of each switch, loaded from settings
    val switches = mutableStateMapOf<ReminderType, Boolean>().apply {
        ReminderType.entries.forEach { put(it, settings.isReminderOn(it.name)) }
    }

    fun setReminder(type: ReminderType, on: Boolean) {
        settings.setReminderOn(type.name, on)
        switches[type] = on
        if (on) ReminderScheduler.schedule(context, type)
        else ReminderScheduler.cancel(context, type)
    }

    fun sendTest() {
        NotificationHelper.show(context, ReminderType.BREATHING)
    }

    fun canNotify(): Boolean = NotificationHelper.canNotify(context)
}
