package com.example.foodyscans.models

data class User(
    var name: String = "",
    var mail: String = "",
    var nutriscore: String = "",
    var novascore: String = "",
    var ecoscore: String = "",
    var allergens: MutableList<String> = mutableListOf(),
    var allergensTags: MutableList<String> = mutableListOf(),
    var diet: String = "no",
    var otherDiets: MutableList<String> = mutableListOf(),
    var nutriments: MutableMap<String, String> = mutableMapOf(
        "fats" to "high",
        "saturated-fats" to "high",
        "sugars" to "high",
        "salt" to "high"
    ),
    var lists: MutableList<ProductList> = mutableListOf(),
    var recipes: MutableList<RecipeList> = mutableListOf()
)