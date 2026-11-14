package com.aslam.healthmate.feature.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aslam.healthmate.ui.components.SingleDatePickerDialog
import com.aslam.healthmate.ui.theme.CaloriesRed
import com.aslam.healthmate.ui.theme.StepsGreen
import com.aslam.healthmate.ui.theme.WaterBlue
import com.aslam.healthmate.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordFormScreen(
    recordId: Int,
    onDone: () -> Unit,
    viewModel: RecordFormViewModel = viewModel()
) {
    LaunchedEffect(recordId) { viewModel.load(recordId) }

    // Close the screen after saving
    LaunchedEffect(viewModel.saved) {
        if (viewModel.saved) onDone()
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val errors = viewModel.errors
    val title = if (recordId > 0) "Edit Record" else "Add Record"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = viewModel.date,
                onValueChange = { viewModel.date = it },
                label = { Text("Date (yyyy-MM-dd)") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Pick date")
                    }
                },
                isError = errors.date != null,
                supportingText = { errors.date?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            NumberField(
                value = viewModel.steps,
                onChange = { viewModel.steps = it },
                label = "Steps",
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                iconColor = StepsGreen,
                error = errors.steps
            )

            NumberField(
                value = viewModel.calories,
                onChange = { viewModel.calories = it },
                label = "Calories burned (kcal)",
                icon = Icons.Filled.LocalFireDepartment,
                iconColor = CaloriesRed,
                error = errors.calories
            )
            TextButton(onClick = { viewModel.estimateCalories() }) {
                Text("Estimate calories from steps")
            }

            NumberField(
                value = viewModel.water,
                onChange = { viewModel.water = it },
                label = "Water (ml)",
                icon = Icons.Filled.WaterDrop,
                iconColor = WaterBlue,
                error = errors.water
            )

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }

    if (showDatePicker) {
        SingleDatePickerDialog(
            initialDate = DateUtils.parse(viewModel.date) ?: DateUtils.today(),
            onDismiss = { showDatePicker = false },
            onDatePicked = { viewModel.date = DateUtils.toText(it) }
        )
    }
}

// Reusable text box for whole numbers, with an error message under it
@Composable
fun NumberField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    error: String?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = iconColor) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
