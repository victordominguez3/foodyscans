package com.example.foodyscans.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenFoodFactsClient {
    private const val BASE_URL = "https://world.openfoodfacts.org/"

    val instance: OpenFoodFactsService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(OpenFoodFactsService::class.java)
    }
}