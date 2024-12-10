package com.example.foodyscans.models

import androidx.room.Entity


@Entity(
    tableName = "ingredients",
    primaryKeys = ["id", "language"]
)
data class IngredientDb(
    val id: String,
    val language: String,
    val name: String
)