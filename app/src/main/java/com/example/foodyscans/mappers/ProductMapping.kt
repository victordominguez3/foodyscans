package com.example.foodyscans.mappers

import com.example.foodyscans.models.Ingredient
import com.example.foodyscans.models.IngredientsAnalysis
import com.example.foodyscans.models.NutrientLevels
import com.example.foodyscans.models.Nutriments
import com.example.foodyscans.models.Product
import com.example.foodyscans.models.Recipe
import java.util.UUID

fun Product.toMap(): Map<String, Any?> {
    val ingredientsMap = ingredients?.map { it.toMap() } ?: emptyList()
    val nutrientLevelsMap = nutrient_levels?.let {
        mapOf(
            "fat" to it.fat,
            "salt" to it.salt,
            "saturated-fat" to it.`saturated-fat`,
            "sugars" to it.sugars
        )
    }
    val ingredientsAnalysisMap = ingredients_analysis?.let {
        mapOf(
            "maybe_vegan" to it.maybeVegan,
            "maybe_vegetarian" to it.maybeVegetarian,
            "non_vegan" to it.nonVegan,
            "non_vegetarian" to it.nonVegetarian,
            "may_contain_palm_oil" to it.mayContainPalmOil,
            "palm_oil" to it.palmOil
        )
    }
    val nutrimentsMap = nutriments?.let {
        mapOf(
            "carbohydrates_100g" to it.carbohydrates_100g,
            "carbohydrates_unit" to it.carbohydrates_unit,
            "energy_kcal_100g" to it.`energy-kcal_100g`,
            "energy_kcal_unit" to it.`energy-kcal_unit`,
            "energy_kj_100g" to it.`energy-kj_100g`,
            "energy_kj_unit" to it.`energy-kj_unit`,
            "fat_100g" to it.fat_100g,
            "fat_unit" to it.fat_unit,
            "salt_100g" to it.salt_100g,
            "salt_unit" to it.salt_unit,
            "saturated_fat_100g" to it.`saturated-fat_100g`,
            "saturated_fat_unit" to it.`saturated-fat_unit`,
            "proteins_100g" to it.proteins_100g,
            "proteins_unit" to it.proteins_unit,
            "sugars_100g" to it.sugars_100g,
            "sugars_unit" to it.sugars_unit,
            "sodium_100g" to it.sodium_100g,
            "sodium_unit" to it.sodium_unit
        )
    }

    return mapOf(
        "id" to (id ?: ""),
        "code" to (code ?: ""),
        "ecoscore_grade" to (ecoscore_grade ?: ""),
        "image_url" to (image_url ?: ""),
        "ingredients" to ingredientsMap,
        "nova_group" to (nova_group ?: 0),
        "nutrient_levels" to nutrientLevelsMap,
        "nutriscore_grade" to (nutriscore_grade ?: ""),
        "product_name" to (product_name ?: ""),
        "product_quantity" to (product_quantity ?: ""),
        "product_quantity_unit" to (product_quantity_unit ?: ""),
        "quantity" to (quantity ?: ""),
        "brands" to (brands ?: ""),
        "traces_tags" to (traces_tags ?: emptyList()),
        "allergens_tags" to (allergens_tags ?: emptyList()),
        "ingredients_analysis" to ingredientsAnalysisMap,
        "nutriments" to nutrimentsMap
    )
}

fun Ingredient.toMap(): Map<String, Any> {
    return mapOf(
        "id" to (id ?: ""),
        "is_in_taxonomy" to (is_in_taxonomy ?: 0),
        "text" to (text ?: ""),
        "vegan" to (vegan ?: ""),
        "vegetarian" to (vegetarian ?: ""),
        "ingredients" to (ingredients?.map { it.toMap() } ?: emptyList())
    )
}

fun Map<*, *>.toProduct(): Product {
    val ingredients = parseIngredients(this["ingredients"])
    val nutrientLevelsData = this["nutrient_levels"] as? Map<*, *>
    val nutrientLevels = nutrientLevelsData?.let {
        NutrientLevels(
            fat = it["fat"] as? String,
            salt = it["salt"] as? String,
            `saturated-fat` = it["saturated-fat"] as? String,
            sugars = it["sugars"] as? String
        )
    }
    val ingredientsAnalysisData = this["ingredients_analysis"] as? Map<*, *>
    val ingredientsAnalysis = ingredientsAnalysisData?.let {
        IngredientsAnalysis(
            maybeVegan = it["maybe_vegan"] as? List<String>,
            maybeVegetarian = it["maybe_vegetarian"] as? List<String>,
            nonVegan = it["non_vegan"] as? List<String>,
            nonVegetarian = it["non_vegetarian"] as? List<String>,
            mayContainPalmOil = it["may_contain_palm_oil"] as? List<String>,
            palmOil = it["palm_oil"] as? List<String>
        )
    }
    val nutrimentsData = this["nutriments"] as? Map<*, *>
    val nutriments = nutrimentsData?.let {
        Nutriments(
            carbohydrates_100g = (it["carbohydrates_100g"] as? Number)?.toDouble(),
            carbohydrates_unit = it["carbohydrates_unit"] as? String,
            `energy-kcal_100g` = (it["energy_kcal_100g"] as? Number)?.toDouble(),
            `energy-kcal_unit` = it["energy_kcal_unit"] as? String,
            `energy-kj_100g` = (it["energy_kj_100g"] as? Number)?.toDouble(),
            `energy-kj_unit` = it["energy_kj_unit"] as? String,
            fat_100g = (it["fat_100g"] as? Number)?.toDouble(),
            fat_unit = it["fat_unit"] as? String,
            salt_100g = (it["salt_100g"] as? Number)?.toDouble(),
            salt_unit = it["salt_unit"] as? String,
            `saturated-fat_100g` = (it["saturated_fat_100g"] as? Number)?.toDouble(),
            `saturated-fat_unit` = it["saturated_fat_unit"] as? String,
            proteins_100g = (it["proteins_100g"] as? Number)?.toDouble(),
            proteins_unit = it["proteins_unit"] as? String,
            sugars_100g = (it["sugars_100g"] as? Number)?.toDouble(),
            sugars_unit = it["sugars_unit"] as? String,
            sodium_100g = (it["sodium_100g"] as? Number)?.toDouble(),
            sodium_unit = it["sodium_unit"] as? String
        )
    }
    return Product(
        id = this["id"] as? String,
        code = this["code"] as? String,
        ecoscore_grade = this["ecoscore_grade"] as? String,
        image_url = this["image_url"] as? String,
        ingredients = ingredients,
        nova_group = (this["nova_group"] as? Number)?.toInt(),
        nutrient_levels = nutrientLevels,
        nutriscore_grade = this["nutriscore_grade"] as? String,
        product_name = this["product_name"] as? String,
        product_quantity = this["product_quantity"] as? String,
        product_quantity_unit = this["product_quantity_unit"] as? String,
        quantity = this["quantity"] as? String,
        brands = this["brands"] as? String,
        traces_tags = this["traces_tags"] as? List<String>,
        allergens_tags = this["allergens_tags"] as? List<String>,
        ingredients_analysis = ingredientsAnalysis,
        nutriments = nutriments
    )
}

fun parseIngredients(ingredientList: Any?): List<Ingredient>? {
    return (ingredientList as? List<Map<*, *>>)?.map { ingredient ->
        Ingredient(
            id = ingredient["id"] as? String,
            is_in_taxonomy = (ingredient["is_in_taxonomy"] as? Number)?.toInt(),
            text = ingredient["text"] as? String,
            vegan = ingredient["vegan"] as? String,
            vegetarian = ingredient["vegetarian"] as? String,
            ingredients = parseIngredients(ingredient["ingredients"])
        )
    }
}

fun Map<*, *>.toRecipe(): Recipe {
    return Recipe(
        id = UUID.fromString(this["id"] as? String),
        title = this["name"] as? String,
        ingredients = this["ingredients"] as? List<String>,
        steps = this["steps"] as? List<String>
    )
}