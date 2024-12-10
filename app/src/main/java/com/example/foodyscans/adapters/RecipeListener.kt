package com.example.foodyscans.adapters

import com.example.foodyscans.models.Recipe

interface RecipeListener {
    fun onClick(recipe: Recipe)
    fun onLongClick(recipe: Recipe)
}