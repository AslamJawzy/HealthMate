package com.aslam.healthmate.feature.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aslam.healthmate.MainActivity
import com.aslam.healthmate.R

object NotificationHelper {

    private const val CHANNEL_ID = "reminders"
    const val EXTRA_OPEN_SCREEN = "open_screen"

    // Android 8+ needs a channel before showing notifications
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, "Health reminders", NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Breathing, water and move reminders"
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission") // checked with canNotify() first
    fun show(context: Context, type: ReminderType) {
        if (!canNotify(context)) return
        createChannel(context)

        // Tapping the notification opens the app. Breathing reminders open the breathing screen.
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (type == ReminderType.BREATHING) putExtra(EXTRA_OPEN_SCREEN, "breathing")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, type.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(type.title)
            .setContentText(type.message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(type.ordinal, notification)
    }
}
