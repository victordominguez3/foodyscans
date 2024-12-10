package com.example.foodyscans.models

import java.util.UUID

data class Recipe(
    val id: UUID?,
    val title: String?,
    val ingredients: List<String>?,
    val steps: List<String>?
)