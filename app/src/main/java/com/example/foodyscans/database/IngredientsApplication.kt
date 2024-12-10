package com.example.foodyscans.database

import android.app.Application
import androidx.room.Room

class IngredientsApplication: Application() {

    companion object{
        lateinit var database: IngredientsDatabase
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(this,
            IngredientsDatabase::class.java,
            "db_ingredients")
            .allowMainThreadQueries().enableMultiInstanceInvalidation().build()
    }

}