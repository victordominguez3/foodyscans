package com.example.foodyscans.api

import com.example.foodyscans.models.ApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsService {
    @GET("api/v2/product/{code}")
    fun getProduct(
        @Path("code") productCode: String,
        @Query("fields") fields: String = "id,ingredients,code,product_name,product_quantity,product_quantity_unit,quantity,nova_group,ecoscore_grade,nutriscore_grade,nutrient_levels,image_url,brands,traces_tags,allergens_tags,ingredients_analysis,nutriments"
    ): Call<ApiResponse>

    @GET("/cgi/search.pl")
    fun getSearchProducts(
        @Query("search_terms") search: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("page") page: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("fields") fields: String = "id,ingredients,code,product_name,product_quantity,product_quantity_unit,quantity,nova_group,ecoscore_grade,nutriscore_grade,nutrient_levels,image_url,brands,traces_tags,allergens_tags,ingredients_analysis,nutriments"
    ): Call<ApiResponse>

}