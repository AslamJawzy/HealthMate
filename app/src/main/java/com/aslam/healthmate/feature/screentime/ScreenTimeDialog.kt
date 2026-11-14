package com.aslam.healthmate.feature.screentime

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

// Friendly break reminder shown after 1 hour of continuous use.
// It only runs while the app is open (see MainActivity), there is no background service.
object ScreenTime {

    // 1 hour. Change to 60_000L (1 minute) to test it quickly.
    const val LIMIT_MILLIS = 60 * 60 * 1000L

    private val messages = listOf(
        "You've been on your phone for a while. Put it aside and rest your eyes.",
        "One hour of screen time! Look at something far away for 20 seconds.",
        "Time for a short break. Stand up, stretch and drink some water.",
        "Your eyes need a rest. Close them for a minute and breathe slowly.",
        "Great job tracking your health! Now take a walk away from the screen."
    )

    fun randomMessage(): String = messages.random()
}

@Composable
fun ScreenTimeDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Visibility, contentDescription = null) },
        title = { Text("Take a break") },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}
