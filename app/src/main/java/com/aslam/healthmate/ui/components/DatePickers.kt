package com.aslam.healthmate.ui.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aslam.healthmate.util.DateUtils
import java.time.LocalDate

// Stops the user picking a day after today
@OptIn(ExperimentalMaterial3Api::class)
private object PastOrToday : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis <= DateUtils.toPickerMillis(DateUtils.today())
    }
}

// Calendar pop-up for one date
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDatePicked: (LocalDate) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.toPickerMillis(initialDate),
        selectableDates = PastOrToday
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) onDatePicked(DateUtils.fromPickerMillis(millis))
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = state)
    }
}

// Calendar pop-up for a start and end date
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onRangePicked: (LocalDate, LocalDate) -> Unit
) {
    val state = rememberDateRangePickerState(selectableDates = PastOrToday)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val start = state.selectedStartDateMillis
                // if only one day was tapped, use it as both start and end
                val end = state.selectedEndDateMillis ?: start
                if (start != null && end != null) {
                    onRangePicked(DateUtils.fromPickerMillis(start), DateUtils.fromPickerMillis(end))
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DateRangePicker(state = state, modifier = Modifier.weight(1f))
    }
}
