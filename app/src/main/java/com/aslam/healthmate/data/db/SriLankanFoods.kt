package com.aslam.healthmate.data.db

// Built-in list of common Sri Lankan foods. Calories are approximate values for
// one typical portion, to give the user a rough idea, not exact nutrition data.
object SriLankanFoods {

    const val MY_FOODS = "My Foods"

    // The order here is the order categories appear in the food picker
    val categories = listOf(
        "Rice & Curry", "Hoppers & Roti", "Curries", "Sambols",
        "Short Eats", "Sweets", "Drinks", "Fruits", MY_FOODS
    )

    private fun f(name: String, category: String, calories: Int, portion: String) =
        Food(name = name, category = category, calories = calories, portion = portion)

    fun list(): List<Food> = listOf(
        // Rice & Curry
        f("Rice and curry (veg)", "Rice & Curry", 600, "1 plate"),
        f("Rice and curry (chicken)", "Rice & Curry", 750, "1 plate"),
        f("Rice and curry (fish)", "Rice & Curry", 700, "1 plate"),
        f("White rice", "Rice & Curry", 205, "1 cup"),
        f("Red rice", "Rice & Curry", 215, "1 cup"),
        f("Fried rice (chicken)", "Rice & Curry", 650, "1 plate"),
        f("Chicken biriyani", "Rice & Curry", 750, "1 plate"),
        f("Yellow rice (kaha bath)", "Rice & Curry", 280, "1 cup"),
        f("Kiribath", "Rice & Curry", 300, "2 pieces"),
        f("Lamprais", "Rice & Curry", 800, "1 packet"),

        // Hoppers & Roti
        f("Plain hopper", "Hoppers & Roti", 90, "1 hopper"),
        f("Egg hopper", "Hoppers & Roti", 165, "1 hopper"),
        f("Milk hopper", "Hoppers & Roti", 130, "1 hopper"),
        f("String hoppers", "Hoppers & Roti", 175, "5 pieces"),
        f("Pittu", "Hoppers & Roti", 230, "1 cup"),
        f("Pol roti", "Hoppers & Roti", 180, "1 roti"),
        f("Godamba roti", "Hoppers & Roti", 250, "1 roti"),
        f("Paratha", "Hoppers & Roti", 260, "1 piece"),
        f("Chicken kottu", "Hoppers & Roti", 800, "1 plate"),
        f("Egg kottu", "Hoppers & Roti", 700, "1 plate"),
        f("Vegetable kottu", "Hoppers & Roti", 550, "1 plate"),
        f("Cheese kottu", "Hoppers & Roti", 950, "1 plate"),
        f("Thosai", "Hoppers & Roti", 120, "1 thosai"),
        f("Masala thosai", "Hoppers & Roti", 350, "1 thosai"),
        f("Idli", "Hoppers & Roti", 140, "2 pieces"),
        f("Bread", "Hoppers & Roti", 160, "2 slices"),

        // Curries
        f("Dhal curry (parippu)", "Curries", 180, "1/2 cup"),
        f("Chicken curry", "Curries", 250, "1 serving"),
        f("Fish curry", "Curries", 200, "1 serving"),
        f("Fish ambul thiyal", "Curries", 180, "1 serving"),
        f("Beef curry", "Curries", 300, "1 serving"),
        f("Mutton curry", "Curries", 320, "1 serving"),
        f("Egg curry", "Curries", 160, "1 egg"),
        f("Potato curry", "Curries", 180, "1/2 cup"),
        f("Jackfruit curry (polos)", "Curries", 150, "1/2 cup"),
        f("Brinjal moju", "Curries", 200, "1/2 cup"),
        f("Green beans curry", "Curries", 90, "1/2 cup"),
        f("Pumpkin curry", "Curries", 120, "1/2 cup"),
        f("Gotukola mallum", "Curries", 60, "1/2 cup"),
        f("Kankun stir fry", "Curries", 80, "1/2 cup"),
        f("Devilled chicken", "Curries", 350, "1 serving"),
        f("Cashew curry", "Curries", 300, "1/2 cup"),

        // Sambols
        f("Pol sambol", "Sambols", 80, "2 tbsp"),
        f("Seeni sambol", "Sambols", 120, "2 tbsp"),
        f("Lunu miris", "Sambols", 30, "1 tbsp"),
        f("Katta sambol", "Sambols", 40, "1 tbsp"),
        f("Coconut chutney", "Sambols", 90, "2 tbsp"),
        f("Papadam", "Sambols", 120, "2 pieces"),

        // Short Eats
        f("Fish bun", "Short Eats", 220, "1 bun"),
        f("Vegetable roti", "Short Eats", 200, "1 piece"),
        f("Fish roll", "Short Eats", 250, "1 roll"),
        f("Chicken roll", "Short Eats", 280, "1 roll"),
        f("Fish cutlet", "Short Eats", 120, "1 cutlet"),
        f("Vegetable patty", "Short Eats", 180, "1 patty"),
        f("Ulundu vadai", "Short Eats", 150, "1 vadai"),
        f("Parippu vadai", "Short Eats", 140, "1 vadai"),
        f("Samosa", "Short Eats", 160, "1 piece"),
        f("Sausage bun", "Short Eats", 300, "1 bun"),

        // Sweets
        f("Wattalapam", "Sweets", 300, "1 cup"),
        f("Kavum", "Sweets", 180, "1 piece"),
        f("Kokis", "Sweets", 140, "2 pieces"),
        f("Aluwa", "Sweets", 160, "2 pieces"),
        f("Kalu dodol", "Sweets", 180, "1 piece"),
        f("Kiri toffee", "Sweets", 120, "2 pieces"),
        f("Mung kavum", "Sweets", 150, "1 piece"),
        f("Love cake", "Sweets", 220, "1 piece"),
        f("Curd and treacle", "Sweets", 300, "1 cup"),
        f("Ice cream", "Sweets", 140, "1 scoop"),

        // Drinks
        f("Plain tea (no sugar)", "Drinks", 2, "1 cup"),
        f("Plain tea with sugar", "Drinks", 40, "1 cup"),
        f("Milk tea", "Drinks", 90, "1 cup"),
        f("Coffee with milk", "Drinks", 100, "1 cup"),
        f("King coconut (thambili)", "Drinks", 60, "1 nut"),
        f("Lime juice", "Drinks", 90, "1 glass"),
        f("Woodapple juice", "Drinks", 180, "1 glass"),
        f("Faluda", "Drinks", 350, "1 glass"),
        f("Milo", "Drinks", 180, "1 cup"),
        f("Ginger beer", "Drinks", 140, "1 bottle"),

        // Fruits
        f("Banana (ambul)", "Fruits", 90, "1 banana"),
        f("Mango", "Fruits", 200, "1 mango"),
        f("Papaya", "Fruits", 60, "1 cup"),
        f("Pineapple", "Fruits", 80, "1 cup"),
        f("Rambutan", "Fruits", 120, "10 pieces"),
        f("Mangosteen", "Fruits", 70, "5 pieces"),
        f("Avocado", "Fruits", 160, "1/2 fruit"),
        f("Watermelon", "Fruits", 46, "1 cup")
    )
}
