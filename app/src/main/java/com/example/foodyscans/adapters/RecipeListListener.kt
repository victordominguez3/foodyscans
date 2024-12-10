package com.example.foodyscans.adapters

import com.example.foodyscans.models.RecipeList

interface RecipeListListener {
    fun onClick(recipeList: RecipeList)
    fun onLongClick(recipeList: RecipeList)
}