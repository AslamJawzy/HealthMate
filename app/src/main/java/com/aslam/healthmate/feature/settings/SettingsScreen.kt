package com.aslam.healthmate.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onRemindersClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show "Saved" messages
    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.message = null
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily goals
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Daily Goals", style = MaterialTheme.typography.titleMedium)
                    SettingsNumberField(viewModel.stepGoal, { viewModel.stepGoal = it }, "Steps")
                    SettingsNumberField(viewModel.calorieGoal, { viewModel.calorieGoal = it }, "Calories burned (kcal)")
                    SettingsNumberField(viewModel.waterGoal, { viewModel.waterGoal = it }, "Water (ml)")
                    viewModel.goalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = { viewModel.saveGoals() }) { Text("Save goals") }
                }
            }

            // Profile, used to work out calories from steps
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Profile", style = MaterialTheme.typography.titleMedium)
                    SettingsNumberField(viewModel.height, { viewModel.height = it }, "Height (cm)")
                    SettingsNumberField(viewModel.weight, { viewModel.weight = it }, "Weight (kg)")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = viewModel.isMale, onClick = { viewModel.isMale = true })
                        Text("Male")
                        RadioButton(selected = !viewModel.isMale, onClick = { viewModel.isMale = false })
                        Text("Female")
                    }
                    viewModel.profileError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = { viewModel.saveProfile() }) { Text("Save profile") }
                }
            }

            // Dark mode and reminders
            Card(Modifier.fillMaxWidth()) {
                Column {
                    ListItem(
                        headlineContent = { Text("Dark mode") },
                        leadingContent = { Icon(Icons.Filled.DarkMode, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = darkMode, onCheckedChange = { viewModel.setDarkMode(it) })
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Reminders") },
                        supportingContent = { Text("Breathing, water and move reminders") },
                        leadingContent = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable(onClick = onRemindersClick)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsNumberField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

