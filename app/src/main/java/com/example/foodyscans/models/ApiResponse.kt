package com.example.foodyscans.models

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    val product: Product? = null,
    val products: List<Product>? = null,
    val count: Int? = null,
    val page: Int? = null,
    val page_count: Int? = null,
    val page_size: Int? = null
)

data class Product(
    val id: String? = null,
    val code: String? = null,
    val ecoscore_grade: String? = null,
    val image_url: String? = null,
    val ingredients: List<Ingredient>? = null,
    val nova_group: Int? = null,
    val nutrient_levels: NutrientLevels? = null,
    val nutriscore_grade: String? = null,
    val product_name: String? = null,
    val product_quantity: String? = null,
    val product_quantity_unit: String? = null,
    val quantity: String? = null,
    val brands: String? = null,
    val traces_tags: List<String>? = null,
    val allergens_tags: List<String>? = null,
    val ingredients_analysis: IngredientsAnalysis? = null,
    val nutriments: Nutriments? = null
)

data class Ingredient(
    val id: String? = null,
    val is_in_taxonomy: Int? = null,
    val text: String? = null,
    val vegan: String? = null,
    val vegetarian: String? = null,
    val ingredients: List<Ingredient>? = null
)

data class NutrientLevels(
    val fat: String? = null,
    val salt: String? = null,
    val `saturated-fat`: String? = null,
    val sugars: String? = null
)

data class IngredientsAnalysis(
    @SerializedName("en:maybe-vegan")
    val maybeVegan: List<String>? = null,

    @SerializedName("en:maybe-vegetarian")
    val maybeVegetarian: List<String>? = null,

    @SerializedName("en:non-vegan")
    val nonVegan: List<String>? = null,

    @SerializedName("en:non-vegetarian")
    val nonVegetarian: List<String>? = null,

    @SerializedName("en:may-contain-palm-oil")
    val mayContainPalmOil: List<String>? = null,

    @SerializedName("en:palm-oil")
    val palmOil: List<String>? = null
)

data class Nutriments(
    val carbohydrates_100g: Double? = null,
    val carbohydrates_unit: String? = null,
    val `energy-kcal_100g`: Double? = null,
    val `energy-kcal_unit`: String? = null,
    val `energy-kj_100g`: Double? = null,
    val `energy-kj_unit`: String? = null,
    val fat_100g: Double? = null,
    val fat_unit: String? = null,
    val salt_100g: Double? = null,
    val salt_unit: String? = null,
    val `saturated-fat_100g`: Double? = null,
    val `saturated-fat_unit`: String? = null,
    val proteins_100g: Double? = null,
    val proteins_unit: String? = null,
    val sugars_100g: Double? = null,
    val sugars_unit: String?,
    val sodium_100g: Double? = null,
    val sodium_unit: String? = null
)