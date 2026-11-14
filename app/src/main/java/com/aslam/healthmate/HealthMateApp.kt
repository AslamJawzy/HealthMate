package com.aslam.healthmate

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aslam.healthmate.feature.breathing.BreathingScreen
import com.aslam.healthmate.feature.dashboard.DashboardScreen
import com.aslam.healthmate.feature.food.FoodPickerScreen
import com.aslam.healthmate.feature.food.FoodScreen
import com.aslam.healthmate.feature.records.RecordFormScreen
import com.aslam.healthmate.feature.reminders.RemindersScreen
import com.aslam.healthmate.feature.records.RecordsScreen
import com.aslam.healthmate.feature.settings.SettingsScreen
import com.aslam.healthmate.feature.sleep.SleepScreen

// All screen names in one place
object Routes {
    const val HOME = "home"
    const val RECORDS = "records"
    const val FOOD = "food"
    const val SLEEP = "sleep"
    const val SETTINGS = "settings"
    const val RECORD_FORM = "record_form/{id}"
    const val BREATHING = "breathing"
    const val REMINDERS = "reminders"
    const val FOOD_PICKER = "food_picker/{date}"
}

data class BottomItem(val route: String, val label: String, val icon: ImageVector)

val bottomItems = listOf(
    BottomItem(Routes.HOME, "Home", Icons.Filled.Home),
    BottomItem(Routes.RECORDS, "Records", Icons.AutoMirrored.Filled.List),
    BottomItem(Routes.FOOD, "Food", Icons.Filled.Restaurant),
    BottomItem(Routes.SLEEP, "Sleep", Icons.Filled.Bedtime),
    BottomItem(Routes.SETTINGS, "Settings", Icons.Filled.Settings)
)

@Composable
fun HealthMateApp(openScreen: String?, onScreenOpened: () -> Unit) {
    val navController = rememberNavController()

    // Opened from a breathing reminder notification
    LaunchedEffect(openScreen) {
        if (openScreen == "breathing") {
            navController.navigate(Routes.BREATHING)
            onScreenOpened()
        }
    }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Only show the bottom bar on the 5 main tabs
    val showBottomBar = bottomItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    // standard bottom-nav setup: don't stack the same tab many times
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            // only bottom padding: each screen's own top bar handles the status bar
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable(Routes.HOME) {
                DashboardScreen(
                    onBreathingClick = { navController.navigate(Routes.BREATHING) }
                )
            }
            composable(Routes.RECORDS) {
                RecordsScreen(
                    onAddClick = { navController.navigate("record_form/0") },
                    onEditClick = { id -> navController.navigate("record_form/$id") }
                )
            }
            composable(
                Routes.RECORD_FORM,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: 0
                RecordFormScreen(recordId = id, onDone = { navController.popBackStack() })
            }
            composable(Routes.FOOD) {
                FoodScreen(onAddFoodClick = { date -> navController.navigate("food_picker/$date") })
            }
            composable(
                Routes.FOOD_PICKER,
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { entry ->
                val date = entry.arguments?.getString("date") ?: ""
                FoodPickerScreen(date = date, onBack = { navController.popBackStack() })
            }
            composable(Routes.SLEEP) { SleepScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(onRemindersClick = { navController.navigate(Routes.REMINDERS) })
            }
            composable(Routes.REMINDERS) { RemindersScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.BREATHING) { BreathingScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

