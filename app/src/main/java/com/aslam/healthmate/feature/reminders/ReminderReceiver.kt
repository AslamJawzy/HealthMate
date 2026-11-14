package com.aslam.healthmate.feature.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aslam.healthmate.data.prefs.SettingsPrefs

// Called by AlarmManager at reminder time
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(ReminderScheduler.EXTRA_TYPE) ?: return
        val type = ReminderType.entries.find { it.name == name } ?: return

        // The user may have switched it off since the alarm was set
        if (!SettingsPrefs.get(context).isReminderOn(type.name)) return

        NotificationHelper.show(context, type)
        ReminderScheduler.schedule(context, type) // set up the next one
    }
}

// Alarms are lost when the phone restarts, so set them again
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderScheduler.rescheduleAll(context)
        }
    }
}
