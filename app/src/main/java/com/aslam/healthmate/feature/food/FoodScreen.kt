package com.aslam.healthmate.feature.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aslam.healthmate.data.db.FoodLog
import com.aslam.healthmate.ui.theme.CaloriesRed
import com.aslam.healthmate.ui.theme.FoodOrange
import com.aslam.healthmate.util.DateUtils

val MEALS = listOf("Breakfast", "Lunch", "Dinner", "Snack")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(
    onAddFoodClick: (String) -> Unit, // passes the date as yyyy-MM-dd
    viewModel: FoodViewModel = viewModel()
) {
    val date by viewModel.date.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val burned by viewModel.burned.collectAsState()
    val eaten = logs.sumOf { it.calories }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Food Diary") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddFoodClick(DateUtils.toText(date)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add food") }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Day selector
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.previousDay() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous day")
                    }
                    Text(
                        if (date == DateUtils.today()) "Today" else DateUtils.pretty(date),
                        Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { viewModel.nextDay() }, enabled = date.isBefore(DateUtils.today())) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next day")
                    }
                }
            }

            // Eaten vs burned
            item { EatenVsBurnedCard(eaten, burned) }

            // Logs grouped by meal
            MEALS.forEach { meal ->
                val mealLogs = logs.filter { it.meal == meal }
                item {
                    Text(
                        "$meal (${mealLogs.sumOf { it.calories }} kcal)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (mealLogs.isEmpty()) {
                    item { Text("Nothing logged", style = MaterialTheme.typography.bodySmall) }
                }
                items(mealLogs, key = { it.id }) { log ->
                    FoodLogItem(log, onDelete = { viewModel.deleteLog(log) })
                }
            }

            // space so the button doesn't cover the last item
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun EatenVsBurnedCard(eaten: Int, burned: Int) {
    val difference = eaten - burned
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Calories eaten vs burned", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Restaurant, contentDescription = null, tint = FoodOrange)
                        Text(" Eaten")
                    }
                    Text("$eaten kcal", color = FoodOrange, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge)
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = CaloriesRed)
                        Text(" Burned")
                    }
                    Text("$burned kcal", color = CaloriesRed, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge)
                }
            }
            val note = if (difference >= 0) "You ate $difference kcal more than you burned through activity"
            else "You burned ${-difference} kcal more than you ate"
            Text(note, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun FoodLogItem(log: FoodLog, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(log.foodName, style = MaterialTheme.typography.bodyLarge)
                Text("${portionText(log.portions)} portion", style = MaterialTheme.typography.bodySmall)
            }
            Text("${log.calories} kcal", color = FoodOrange, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

// 1.0 -> "1", 0.5 -> "0.5"
fun portionText(portions: Double): String =
    if (portions % 1.0 == 0.0) portions.toInt().toString() else portions.toString()
