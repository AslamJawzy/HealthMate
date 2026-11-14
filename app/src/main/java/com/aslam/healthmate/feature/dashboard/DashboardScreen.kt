package com.aslam.healthmate.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.aslam.healthmate.feature.food.EatenVsBurnedCard
import com.aslam.healthmate.feature.steps.StepTracker
import com.aslam.healthmate.ui.components.BarChart
import com.aslam.healthmate.ui.components.StatCard
import com.aslam.healthmate.ui.components.formatNumber
import com.aslam.healthmate.ui.theme.BreathCyan
import com.aslam.healthmate.ui.theme.CaloriesRed
import com.aslam.healthmate.ui.theme.StepsGreen
import com.aslam.healthmate.ui.theme.WaterBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBreathingClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val period by viewModel.period.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val stepGoal by viewModel.stepGoal.collectAsState()
    val calorieGoal by viewModel.calorieGoal.collectAsState()
    val waterGoal by viewModel.waterGoal.collectAsState()
    // read date so the screen redraws when it changes
    viewModel.date.collectAsState().value
    val last7Days by viewModel.last7Days.collectAsState()
    val chartMetric by viewModel.chartMetric.collectAsState()
    val stepStatus by StepTracker.status.collectAsState()
    val caloriesEaten by viewModel.caloriesEaten.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HealthMate") },
                actions = {
                    IconButton(onClick = onBreathingClick) {
                        Icon(Icons.Filled.SelfImprovement, contentDescription = "Breathing exercise", tint = BreathCyan)
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
            // Day / Week / Month selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = period == Period.DAY,
                    onClick = { viewModel.setPeriod(Period.DAY) },
                    label = { Text("Day") }
                )
                FilterChip(
                    selected = period == Period.WEEK,
                    onClick = { viewModel.setPeriod(Period.WEEK) },
                    label = { Text("Week") }
                )
                FilterChip(
                    selected = period == Period.MONTH,
                    onClick = { viewModel.setPeriod(Period.MONTH) },
                    label = { Text("Month") }
                )
            }

            // Previous / next arrows with the date label in the middle
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.previous() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
                }
                Text(
                    summary.label,
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { viewModel.next() }, enabled = viewModel.canGoNext()) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
                }
            }

            if (period == Period.DAY) {
                StatCard("Steps", summary.totalSteps, "steps", stepGoal, StepsGreen, Icons.AutoMirrored.Filled.DirectionsWalk)
                StatCard("Calories Burned", summary.totalCalories, "kcal", calorieGoal, CaloriesRed, Icons.Filled.LocalFireDepartment)
                StatCard("Water Intake", summary.totalWater, "ml", waterGoal, WaterBlue, Icons.Filled.WaterDrop)
            } else {
                // Week and month: the bar shows the daily average against the daily goal
                val daysText = "${summary.days} days recorded"
                StatCard(
                    "Steps (daily average)", summary.avgSteps, "steps", stepGoal, StepsGreen,
                    Icons.AutoMirrored.Filled.DirectionsWalk,
                    subtitle = "Total ${formatNumber(summary.totalSteps)} steps, $daysText"
                )
                StatCard(
                    "Calories (daily average)", summary.avgCalories, "kcal", calorieGoal, CaloriesRed,
                    Icons.Filled.LocalFireDepartment,
                    subtitle = "Total ${formatNumber(summary.totalCalories)} kcal, $daysText"
                )
                StatCard(
                    "Water (daily average)", summary.avgWater, "ml", waterGoal, WaterBlue,
                    Icons.Filled.WaterDrop,
                    subtitle = "Total ${formatNumber(summary.totalWater)} ml, $daysText"
                )
            }

            // Food diary vs activity, for the chosen day
            if (period == Period.DAY) {
                EatenVsBurnedCard(eaten = caloriesEaten, burned = summary.totalCalories)
            }

            // Live step counter status
            if (period == Period.DAY) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = StepsGreen)
                    Text(" $stepStatus", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Quick add water for today
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Quick add water (today)", style = MaterialTheme.typography.titleMedium)
                    Row(
                        Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { viewModel.addWater(250) }) {
                            Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = WaterBlue)
                            Text(" +250 ml")
                        }
                        OutlinedButton(onClick = { viewModel.addWater(500) }) {
                            Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = WaterBlue)
                            Text(" +500 ml")
                        }
                    }
                }
            }

            // Weekly bar chart with goal line
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Last 7 days", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = chartMetric == ChartMetric.STEPS,
                            onClick = { viewModel.setChartMetric(ChartMetric.STEPS) },
                            label = { Text("Steps") }
                        )
                        FilterChip(
                            selected = chartMetric == ChartMetric.CALORIES,
                            onClick = { viewModel.setChartMetric(ChartMetric.CALORIES) },
                            label = { Text("Calories") }
                        )
                        FilterChip(
                            selected = chartMetric == ChartMetric.WATER,
                            onClick = { viewModel.setChartMetric(ChartMetric.WATER) },
                            label = { Text("Water") }
                        )
                    }
                    val values = last7Days.map { day ->
                        when (chartMetric) {
                            ChartMetric.STEPS -> day.record?.steps ?: 0
                            ChartMetric.CALORIES -> day.record?.calories ?: 0
                            ChartMetric.WATER -> day.record?.water ?: 0
                        }
                    }
                    val goal = when (chartMetric) {
                        ChartMetric.STEPS -> stepGoal
                        ChartMetric.CALORIES -> calorieGoal
                        ChartMetric.WATER -> waterGoal
                    }
                    val color = when (chartMetric) {
                        ChartMetric.STEPS -> StepsGreen
                        ChartMetric.CALORIES -> CaloriesRed
                        ChartMetric.WATER -> WaterBlue
                    }
                    if (values.isNotEmpty()) {
                        BarChart(
                            values = values,
                            labels = last7Days.map { it.date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() } },
                            goal = goal,
                            barColor = color,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Text("Dashed line = daily goal", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
