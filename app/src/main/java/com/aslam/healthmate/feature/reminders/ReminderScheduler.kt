package com.aslam.healthmate.feature.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.aslam.healthmate.data.prefs.SettingsPrefs
import com.aslam.healthmate.util.ReminderTimes
import java.time.LocalDateTime
import java.time.ZoneId

// Sets one alarm for the next reminder time. When it goes off, ReminderReceiver
// shows the notification and calls schedule() again for the time after that.
object ReminderScheduler {

    const val EXTRA_TYPE = "reminder_type"

    // Android may deliver the alarm up to 5 minutes late to save battery.
    // This avoids needing the special "exact alarm" permission.
    private const val WINDOW_MILLIS = 5 * 60 * 1000L

    fun schedule(context: Context, type: ReminderType) {
        val next = ReminderTimes.next(LocalDateTime.now(), type.times)
        val millis = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, millis, WINDOW_MILLIS, pendingIntent(context, type))
    }

    fun cancel(context: Context, type: ReminderType) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent(context, type))
    }

    // Used after a phone restart (alarms are cleared) and when the app opens
    fun rescheduleAll(context: Context) {
        val settings = SettingsPrefs.get(context)
        ReminderType.entries.forEach { type ->
            if (settings.isReminderOn(type.name)) schedule(context, type)
        }
    }

    private fun pendingIntent(context: Context, type: ReminderType): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, type.name)
        }
        return PendingIntent.getBroadcast(
            context, type.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
