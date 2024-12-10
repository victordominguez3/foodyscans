package com.example.foodyscans.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.foodyscans.models.IngredientDb


@Database(entities = [IngredientDb::class], version = 1)
abstract class IngredientsDatabase: RoomDatabase() {
    abstract fun IngredientDao(): IngredientDao
}