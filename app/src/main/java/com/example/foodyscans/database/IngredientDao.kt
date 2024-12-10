package com.example.foodyscans.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.foodyscans.models.IngredientDb


@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredients WHERE id = :id AND language = :language")
    fun getNameByIdAndLanguage(id: String, language: String): IngredientDb?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(names: List<IngredientDb>)

    @Query("SELECT COUNT(*) FROM ingredients")
    suspend fun getCount(): Int

    @Query("DELETE FROM ingredients")
    fun deleteAllIngredients()
}