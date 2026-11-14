package com.aslam.healthmate.feature.food

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aslam.healthmate.data.db.Food
import com.aslam.healthmate.data.db.SriLankanFoods
import com.aslam.healthmate.ui.theme.CaloriesRed
import com.aslam.healthmate.ui.theme.FoodOrange
import java.time.LocalTime
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodPickerScreen(
    date: String,
    onBack: () -> Unit,
    viewModel: FoodPickerViewModel = viewModel()
) {
    val foods by viewModel.foods.collectAsState()
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    var foodToDelete by remember { mutableStateOf<Food?>(null) }

    // Go back to the diary once a food has been added
    LaunchedEffect(viewModel.logged) {
        if (viewModel.logged) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Food") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openNewFood() }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add custom food")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = viewModel.search,
                onValueChange = { viewModel.search = it },
                placeholder = { Text("Search foods") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val searchText = viewModel.search.trim()

                if (searchText.isNotEmpty()) {
                    // Search results
                    val results = foods.filter { it.name.contains(searchText, ignoreCase = true) }
                    if (results.isEmpty()) {
                        item {
                            Column {
                                Text("No food called \"$searchText\" in the list.")
                                Button(onClick = { viewModel.openNewFood() }, Modifier.padding(top = 8.dp)) {
                                    Text("Add it as a custom food")
                                }
                            }
                        }
                    }
                    items(results, key = { "search_${it.id}" }) { food ->
                        FoodRow(food, { selectedFood = food }, { viewModel.toggleFavourite(food) },
                            { viewModel.openEditFood(food) }, { foodToDelete = food })
                    }
                } else {
                    // Favourites at the top
                    val favourites = foods.filter { it.isFavourite }
                    item { CategoryHeader("Favourites") }
                    if (favourites.isEmpty()) {
                        item {
                            Text(
                                "Tap the heart on any food to pin it here.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    items(favourites, key = { "fav_${it.id}" }) { food ->
                        FoodRow(food, { selectedFood = food }, { viewModel.toggleFavourite(food) },
                            { viewModel.openEditFood(food) }, { foodToDelete = food })
                    }

                    // Then every category in order
                    SriLankanFoods.categories.forEach { category ->
                        val inCategory = foods.filter { it.category == category }
                        if (inCategory.isNotEmpty()) {
                            item { CategoryHeader(category) }
                            items(inCategory, key = { "cat_${it.id}" }) { food ->
                                FoodRow(food, { selectedFood = food }, { viewModel.toggleFavourite(food) },
                                    { viewModel.openEditFood(food) }, { foodToDelete = food })
                            }
                        }
                    }
                }
            }
        }
    }

    // Pop-ups
    selectedFood?.let { food ->
        LogFoodDialog(
            food = food,
            onDismiss = { selectedFood = null },
            onAdd = { meal, portions ->
                viewModel.logFood(date, food, meal, portions)
                selectedFood = null
            }
        )
    }

    if (viewModel.showForm) {
        CustomFoodDialog(viewModel)
    }

    foodToDelete?.let { food ->
        AlertDialog(
            onDismissRequest = { foodToDelete = null },
            title = { Text("Delete food?") },
            text = { Text("\"${food.name}\" will be removed from your food list. Your diary entries stay.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteFood(food)
                    foodToDelete = null
                }) { Text("Delete", color = CaloriesRed) }
            },
            dismissButton = {
                TextButton(onClick = { foodToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CategoryHeader(title: String) {
    Column(Modifier.padding(top = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        HorizontalDivider(Modifier.padding(top = 4.dp))
    }
}

@Composable
fun FoodRow(
    food: Food,
    onClick: () -> Unit,
    onFavourite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.bodyLarge)
                val custom = if (food.isCustom) "  (my food)" else ""
                Text(
                    "${food.portion}, ${food.calories} kcal$custom",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            // Only the user's own foods can be edited or deleted
            if (food.isCustom) {
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
            }
            IconButton(onClick = onFavourite) {
                if (food.isFavourite) {
                    Icon(Icons.Filled.Favorite, contentDescription = "Remove from favourites", tint = CaloriesRed)
                } else {
                    Icon(Icons.Filled.FavoriteBorder, contentDescription = "Add to favourites")
                }
            }
        }
    }
}

// Guess the meal from the time of day, so the user usually doesn't need to change it
fun mealForNow(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 11 -> "Breakfast"
        hour < 16 -> "Lunch"
        hour < 22 -> "Dinner"
        else -> "Snack"
    }
}

@Composable
fun LogFoodDialog(food: Food, onDismiss: () -> Unit, onAdd: (String, Double) -> Unit) {
    var meal by remember { mutableStateOf(mealForNow()) }
    var portions by remember { mutableDoubleStateOf(1.0) }
    val total = (food.calories * portions).roundToInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(food.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1 portion = ${food.portion} (${food.calories} kcal)")

                Text("Meal", fontWeight = FontWeight.SemiBold)
                // two rows of two so they fit on small screens
                MEALS.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { m ->
                            FilterChip(selected = meal == m, onClick = { meal = m }, label = { Text(m) })
                        }
                    }
                }

                Text("Portions", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.5, 1.0, 1.5, 2.0).forEach { p ->
                        FilterChip(
                            selected = portions == p,
                            onClick = { portions = p },
                            label = { Text(portionText(p)) }
                        )
                    }
                }

                Text("Total: $total kcal", color = FoodOrange, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
            }
        },
        confirmButton = { Button(onClick = { onAdd(meal, portions) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CustomFoodDialog(viewModel: FoodPickerViewModel) {
    val errors = viewModel.formErrors
    var categoryMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { viewModel.closeForm() },
        title = { Text(if (viewModel.isEditing) "Edit food" else "Add custom food") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.formName,
                    onValueChange = { viewModel.formName = it },
                    label = { Text("Food name") },
                    isError = errors.name != null,
                    supportingText = { errors.name?.let { Text(it) } },
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.formCalories,
                    onValueChange = { viewModel.formCalories = it },
                    label = { Text("Calories per portion (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errors.calories != null,
                    supportingText = { errors.calories?.let { Text(it) } },
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.formPortion,
                    onValueChange = { viewModel.formPortion = it },
                    label = { Text("Portion (e.g. 1 cup)") },
                    isError = errors.portion != null,
                    supportingText = { errors.portion?.let { Text(it) } },
                    singleLine = true
                )

                // Category dropdown
                Text("Category", fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(onClick = { categoryMenuOpen = true }) {
                        Text(viewModel.formCategory)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = categoryMenuOpen, onDismissRequest = { categoryMenuOpen = false }) {
                        SriLankanFoods.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    viewModel.formCategory = category
                                    categoryMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { viewModel.saveFood() }) { Text("Save") } },
        dismissButton = { TextButton(onClick = { viewModel.closeForm() }) { Text("Cancel") } }
    )
}
