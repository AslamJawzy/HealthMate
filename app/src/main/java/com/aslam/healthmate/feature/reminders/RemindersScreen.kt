package com.aslam.healthmate.feature.reminders

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aslam.healthmate.ui.theme.BreathCyan
import com.aslam.healthmate.ui.theme.StepsGreen
import com.aslam.healthmate.ui.theme.WaterBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onBack: () -> Unit,
    viewModel: RemindersViewModel = viewModel()
) {
    var permissionDenied by remember { mutableStateOf(!viewModel.canNotify()) }
    // What to do once the user answers the permission pop-up
    var afterPermission by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionDenied = !granted
        if (granted) afterPermission?.invoke()
        afterPermission = null
    }

    // Android 13+ asks the user before an app can show notifications
    fun withPermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !viewModel.canNotify()) {
            afterPermission = action
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            action()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (permissionDenied) {
                Text(
                    "Notifications are blocked. Turn on a reminder to allow them, or enable them in the phone's app settings.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Card(Modifier.fillMaxWidth()) {
                ReminderType.entries.forEach { type ->
                    val (icon, color) = when (type) {
                        ReminderType.BREATHING -> Pair(Icons.Filled.SelfImprovement, BreathCyan)
                        ReminderType.WATER -> Pair(Icons.Filled.WaterDrop, WaterBlue)
                        ReminderType.MOVE -> Pair(Icons.AutoMirrored.Filled.DirectionsWalk, StepsGreen)
                    }
                    ListItem(
                        headlineContent = { Text(type.title) },
                        supportingContent = { Text(type.schedule) },
                        leadingContent = { Icon(icon, contentDescription = null, tint = color) },
                        trailingContent = {
                            Switch(
                                checked = viewModel.switches[type] == true,
                                onCheckedChange = { on ->
                                    if (on) withPermission { viewModel.setReminder(type, true) }
                                    else viewModel.setReminder(type, false)
                                }
                            )
                        }
                    )
                }
            }

            Text(
                "Reminders may arrive a few minutes late because Android groups alarms to save battery.",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedButton(
                onClick = { withPermission { viewModel.sendTest() } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send test notification")
            }
        }
    }
}
