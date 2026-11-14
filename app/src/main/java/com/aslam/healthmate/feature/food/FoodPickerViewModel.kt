package com.aslam.healthmate.feature.food

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aslam.healthmate.data.db.AppDatabase
import com.aslam.healthmate.data.db.Food
import com.aslam.healthmate.data.db.FoodLog
import com.aslam.healthmate.data.db.SriLankanFoods
import com.aslam.healthmate.data.repository.FoodRepository
import com.aslam.healthmate.util.FoodErrors
import com.aslam.healthmate.util.Validator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Food picker: search, favourites, categories, and adding/editing custom foods
class FoodPickerViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getDatabase(app)
    private val repo = FoodRepository(db.foodDao(), db.foodLogDao())

    val foods: StateFlow<List<Food>> = repo.allFoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var search by mutableStateOf("")

    // Becomes true after a food is logged, so the screen can go back
    var logged by mutableStateOf(false)
        private set

    fun toggleFavourite(food: Food) {
        viewModelScope.launch { repo.setFavourite(food, !food.isFavourite) }
    }

    fun logFood(date: String, food: Food, meal: String, portions: Double) {
        viewModelScope.launch {
            repo.addLog(
                FoodLog(
                    date = date,
                    meal = meal,
                    foodName = food.name,
                    portions = portions,
                    calories = (food.calories * portions).roundToInt()
                )
            )
            logged = true
        }
    }

    // ----- Custom food form -----
    var showForm by mutableStateOf(false)
        private set
    private var editingFood: Food? = null
    val isEditing: Boolean get() = editingFood != null

    var formName by mutableStateOf("")
    var formCalories by mutableStateOf("")
    var formPortion by mutableStateOf("1 serving")
    var formCategory by mutableStateOf(SriLankanFoods.MY_FOODS)
    var formErrors by mutableStateOf(FoodErrors())
        private set

    fun openNewFood() {
        editingFood = null
        formName = search.trim() // handy if the user searched and found nothing
        formCalories = ""
        formPortion = "1 serving"
        formCategory = SriLankanFoods.MY_FOODS
        formErrors = FoodErrors()
        showForm = true
    }

    fun openEditFood(food: Food) {
        editingFood = food
        formName = food.name
        formCalories = food.calories.toString()
        formPortion = food.portion
        formCategory = food.category
        formErrors = FoodErrors()
        showForm = true
    }

    fun closeForm() {
        showForm = false
    }

    fun saveFood() {
        viewModelScope.launch {
            val sameName = repo.findByName(formName)
            val nameUsed = sameName != null && sameName.id != editingFood?.id
            val result = Validator.validateFood(formName, formCalories, formPortion, nameUsed)
            formErrors = result
            if (result.hasErrors) return@launch

            val old = editingFood
            if (old == null) {
                repo.addFood(
                    Food(
                        name = formName.trim(),
                        category = formCategory,
                        calories = formCalories.trim().toInt(),
                        portion = formPortion.trim(),
                        isCustom = true
                    )
                )
            } else {
                repo.updateFood(
                    old.copy(
                        name = formName.trim(),
                        category = formCategory,
                        calories = formCalories.trim().toInt(),
                        portion = formPortion.trim()
                    )
                )
            }
            showForm = false
        }
    }

    // Only custom foods can be deleted (the screen only shows the button for those)
    fun deleteFood(food: Food) {
        if (!food.isCustom) return
        viewModelScope.launch { repo.deleteFood(food) }
    }
}
