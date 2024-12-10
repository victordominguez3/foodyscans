package com.example.foodyscans.models

data class RecipeList(
    val name: String,
    val recipes: MutableList<Recipe>
)