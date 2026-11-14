package com.aslam.healthmate.feature.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aslam.healthmate.data.db.HealthRecord
import com.aslam.healthmate.ui.components.DateRangePickerDialog
import com.aslam.healthmate.ui.components.MetricChip
import com.aslam.healthmate.ui.components.SingleDatePickerDialog
import com.aslam.healthmate.ui.components.formatNumber
import com.aslam.healthmate.ui.theme.CaloriesRed
import com.aslam.healthmate.ui.theme.StepsGreen
import com.aslam.healthmate.ui.theme.WaterBlue
import com.aslam.healthmate.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    onAddClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    viewModel: RecordsViewModel = viewModel()
) {
    val records by viewModel.records.collectAsState()
    val filter by viewModel.filter.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showRangePicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Health Records") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add record")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Search buttons
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { showDatePicker = true },
                    label = { Text("Search date") },
                    leadingIcon = { Icon(Icons.Filled.Event, contentDescription = null) }
                )
                AssistChip(
                    onClick = { showRangePicker = true },
                    label = { Text("Date range") },
                    leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) }
                )
            }

            // Shows what is being searched, with a button to clear it
            val f = filter
            if (f != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val text = if (f.from == f.to) "Showing ${DateUtils.pretty(f.from)}"
                    else "Showing ${DateUtils.short(f.from)} to ${DateUtils.short(f.to)}"
                    Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear search")
                    }
                }
            }

            if (records.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No records found")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        RecordItem(
                            record = record,
                            onEdit = { onEditClick(record.id) },
                            onDelete = {
                                viewModel.delete(record)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Record deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoDelete()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        SingleDatePickerDialog(
            initialDate = DateUtils.today(),
            onDismiss = { showDatePicker = false },
            onDatePicked = { viewModel.searchDate(it) }
        )
    }
    if (showRangePicker) {
        DateRangePickerDialog(
            onDismiss = { showRangePicker = false },
            onRangePicked = { from, to -> viewModel.searchRange(from, to) }
        )
    }
}

@Composable
fun RecordItem(record: HealthRecord, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    DateUtils.pretty(record.date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricChip(Icons.AutoMirrored.Filled.DirectionsWalk, formatNumber(record.steps), StepsGreen)
                    MetricChip(Icons.Filled.LocalFireDepartment, "${record.calories} kcal", CaloriesRed)
                    MetricChip(Icons.Filled.WaterDrop, "${record.water} ml", WaterBlue)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}
