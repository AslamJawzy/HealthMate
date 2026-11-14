package com.aslam.healthmate.feature.sleep

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aslam.healthmate.data.db.SleepLog
import com.aslam.healthmate.ui.components.SingleDatePickerDialog
import com.aslam.healthmate.ui.theme.SleepIndigo
import com.aslam.healthmate.util.DateUtils
import com.aslam.healthmate.util.SleepCalc
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(viewModel: SleepViewModel = viewModel()) {
    val logs by viewModel.logs.collectAsState()
    val weeklyAverage by viewModel.weeklyAverage.collectAsState()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.message = null
        }
    }

    // Android's built-in clock picker
    fun pickTime(start: LocalTime, onPicked: (LocalTime) -> Unit) {
        TimePickerDialog(context, { _, hour, minute ->
            onPicked(LocalTime.of(hour, minute))
        }, start.hour, start.minute, true).show()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sleep Tracker") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Weekly average
            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Bedtime, contentDescription = null, tint = SleepIndigo)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text("Average sleep (last 7 days)", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (weeklyAverage > 0) SleepCalc.format(weeklyAverage) else "No data yet",
                                style = MaterialTheme.typography.headlineSmall,
                                color = SleepIndigo,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Adults need about 7 to 9 hours", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Log a night
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Log sleep", style = MaterialTheme.typography.titleMedium)
                        OutlinedButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                            Text("  Woke up on ${DateUtils.pretty(viewModel.date)}")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { pickTime(viewModel.bedTime) { viewModel.bedTime = it } }) {
                                Icon(Icons.Filled.Bedtime, contentDescription = null, tint = SleepIndigo)
                                Text("  Bed ${viewModel.formatTime(viewModel.bedTime)}")
                            }
                            OutlinedButton(onClick = { pickTime(viewModel.wakeTime) { viewModel.wakeTime = it } }) {
                                Icon(Icons.Filled.WbSunny, contentDescription = null)
                                Text("  Wake ${viewModel.formatTime(viewModel.wakeTime)}")
                            }
                        }
                        Text("Hours slept: ${viewModel.preview()}", fontWeight = FontWeight.SemiBold)
                        viewModel.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Button(
                            onClick = { viewModel.save() },
                            colors = ButtonDefaults.buttonColors(containerColor = SleepIndigo)
                        ) { Text("Save sleep") }
                    }
                }
            }

            item { Text("History", style = MaterialTheme.typography.titleMedium) }
            if (logs.isEmpty()) {
                item { Text("No sleep logged yet") }
            }
            items(logs, key = { it.id }) { log ->
                SleepItem(log, onDelete = { viewModel.delete(log) })
            }
        }
    }

    if (showDatePicker) {
        SingleDatePickerDialog(
            initialDate = viewModel.date,
            onDismiss = { showDatePicker = false },
            onDatePicked = { viewModel.date = it }
        )
    }
}

@Composable
fun SleepItem(log: SleepLog, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(DateUtils.pretty(log.date), style = MaterialTheme.typography.bodyLarge)
                Text("${log.bedTime} to ${log.wakeTime}", style = MaterialTheme.typography.bodySmall)
            }
            Text(SleepCalc.format(log.minutes), color = SleepIndigo, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}
