package com.example.foodyscans.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.foodyscans.R
import com.example.foodyscans.api.OpenAIClient
import com.example.foodyscans.database.IngredientsApplication.Companion.database
import com.example.foodyscans.databinding.FragmentProductBinding
import com.example.foodyscans.models.ChatRequest
import com.example.foodyscans.models.ChatResponse
import com.example.foodyscans.models.Ingredient
import com.example.foodyscans.models.Message
import com.example.foodyscans.models.NutrientLevels
import com.example.foodyscans.models.Product
import com.example.foodyscans.models.ProductList
import com.example.foodyscans.models.Recipe
import com.example.foodyscans.models.RecipeContent
import com.example.foodyscans.models.User
import com.example.foodyscans.models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class ProductFragment(private val product: Product) : DialogFragment() {

    private lateinit var binding: FragmentProductBinding
    private val userViewModel: UserViewModel by activityViewModels()

    private var user = User()

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout((resources.displayMetrics.widthPixels*0.9).toInt(), LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            this.user = user
            if (user.lists.find { it.name == "Favoritos" }?.products?.any { it.id == product.id } == true) binding.fav.setBackgroundResource(R.drawable.fav) else binding.fav.setBackgroundResource(R.drawable.not_fav)
            if (user.lists.filter { it.name != "Favoritos" }.any { it -> it.products.any { it.id == product.id } }) binding.save.setBackgroundResource(R.drawable.save) else binding.save.setBackgroundResource(R.drawable.not_save)
            if (isApt()) {
                binding.aptText.text = "APTO"
                binding.aptText.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            } else {
                binding.aptText.text = "NO APTO"
                binding.aptText.setTextColor(ContextCompat.getColor(requireContext(), R.color.nutriments_high))
            }
        })

        try {
            Glide.with(binding.image)
                .load(product.image_url ?: "")
                .transform(CenterCrop(), RoundedCorners(20))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.image)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.nameTitle.text = if (product.product_name.isNullOrBlank()) "Sin nombre" else product.product_name
        binding.name.text = "Nombre: ${if (product.product_name.isNullOrBlank()) "Sin nombre" else product.product_name}"
        binding.barcode.text = "Código de barras: ${product.code ?: "Sin código de barras"}"
        binding.brands.text = "Marca: ${if (product.brands.isNullOrBlank()) "Sin marca" else product.brands}"
        binding.quantity.text = "Cantidad: ${if (product.quantity.isNullOrBlank()) "Sin cantidad" else product.quantity}"

        val ingredientsText = StringBuilder()
        createIngredientsText(product.ingredients ?: emptyList(), ingredientsText)
        if (ingredientsText.isNotEmpty()) {
            ingredientsText.setLength(ingredientsText.length - 2)
        }

        binding.ingredients.text = "Ingredientes: ${ingredientsText.ifEmpty { "Sin ingredientes" }}"

        when (product.nutriscore_grade ?: "") {
            "a" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_a)
            "b" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_b)
            "c" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_c)
            "d" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_d)
            "e" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_e)
            else -> {
                binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_a)
                val matrix = ColorMatrix()
                matrix.setSaturation(0f)
                val filter = ColorMatrixColorFilter(matrix)
                binding.nutriscoreImage.colorFilter = filter
            }
        }
        when (product.nova_group ?: "") {
            1 -> binding.novaImage.setImageResource(R.drawable.nova_group_1)
            2 -> binding.novaImage.setImageResource(R.drawable.nova_group_2)
            3 -> binding.novaImage.setImageResource(R.drawable.nova_group_3)
            4 -> binding.novaImage.setImageResource(R.drawable.nova_group_4)
            else -> {
                binding.novaImage.setImageResource(R.drawable.nova_group_1)
                val matrix = ColorMatrix()
                matrix.setSaturation(0f)
                val filter = ColorMatrixColorFilter(matrix)
                binding.novaImage.colorFilter = filter
            }
        }
        when (product.ecoscore_grade ?: "") {
            "a" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_a)
            "b" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_b)
            "c" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_c)
            "d" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_d)
            "e" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_e)
            else -> {
                binding.ecoscoreImage.setImageResource(R.drawable.eco_score_a)
                val matrix = ColorMatrix()
                matrix.setSaturation(0f)
                val filter = ColorMatrixColorFilter(matrix)
                binding.ecoscoreImage.colorFilter = filter
            }
        }

        val nutriments = product.nutriments

        binding.energykcalText.text = nutriments?.`energy-kcal_100g`.roundTo(2) + " " + (nutriments?.`energy-kcal_unit` ?: "")
        binding.energykjText.text = nutriments?.`energy-kj_100g`.roundTo(2) + " " + (nutriments?.`energy-kj_unit` ?: "")
        binding.carbohydratesText.text = nutriments?.carbohydrates_100g.roundTo(2) + " " + (nutriments?.carbohydrates_unit ?: "")
        binding.proteinsText.text = nutriments?.proteins_100g.roundTo(2) + " " + (nutriments?.proteins_unit ?: "")
        binding.sodiumText.text = nutriments?.sodium_100g.roundTo(2) + " " + (nutriments?.sodium_unit ?: "")

        binding.fatText.text = nutriments?.fat_100g.roundTo(2) + " " + (nutriments?.fat_unit ?: "")
        var shapeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.normal_table) as GradientDrawable
        shapeDrawable.setColor(ContextCompat.getColor(requireContext(), getColor(product.nutrient_levels?.fat.toString())))
        binding.fatsContainer.background = shapeDrawable

        binding.saturatedFatsText.text = nutriments?.`saturated-fat_100g`.roundTo(2) + " " + (nutriments?.`saturated-fat_unit` ?: "")
        shapeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.normal_table) as GradientDrawable
        shapeDrawable.setColor(ContextCompat.getColor(requireContext(), getColor(product.nutrient_levels?.`saturated-fat`.toString())))
        binding.saturatedFatsContainer.background = shapeDrawable

        binding.sugarsText.text = nutriments?.sugars_100g.roundTo(2) + " " + (nutriments?.sugars_unit ?: "")
        shapeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.normal_table) as GradientDrawable
        shapeDrawable.setColor(ContextCompat.getColor(requireContext(), getColor(product.nutrient_levels?.sugars.toString())))
        binding.sugarsContainer.background = shapeDrawable

        binding.saltsText.text = nutriments?.salt_100g.roundTo(2) + " " + (nutriments?.salt_unit ?: "")
        shapeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bottom_left_table) as GradientDrawable
        shapeDrawable.setColor(ContextCompat.getColor(requireContext(), getColor(product.nutrient_levels?.salt.toString())))
        binding.saltsContainer.background = shapeDrawable

        shapeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.normal_table) as GradientDrawable
        shapeDrawable.setColor(ContextCompat.getColor(requireContext(), R.color.almost_white))
        shapeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bottom_left_table) as GradientDrawable
        shapeDrawable.setColor(ContextCompat.getColor(requireContext(), R.color.almost_white))

        binding.fav.setOnClickListener {
            if (binding.fav.background.constantState == ContextCompat.getDrawable(requireContext(), R.drawable.not_fav)?.constantState) {
                binding.fav.setBackgroundResource(R.drawable.fav)
                userViewModel.saveProductToFav(product) { success ->
                    if (!success) {
                        Toast.makeText(requireContext(), "Error al guardar el producto", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                binding.fav.setBackgroundResource(R.drawable.not_fav)
                userViewModel.removeProductFromFav(product) { success ->
                    if (!success) {
                        Toast.makeText(requireContext(), "Error al eliminar el producto", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.save.setOnClickListener {
            val dialogFragment = SaveToListFragment(product)
            dialogFragment.show(parentFragmentManager, "")
        }
    }

    private fun Double?.roundTo(decimals: Int): String {
        return this?.let { "%.${decimals}f".format(it) } ?: "Desconocido"
    }

    private fun isApt(): Boolean {

        var result = true

        if (!product.allergens_tags.isNullOrEmpty() && product.allergens_tags.intersect(user.allergensTags).isNotEmpty()) result = false
        if (!product.traces_tags.isNullOrEmpty() && product.traces_tags.intersect(user.allergensTags).isNotEmpty()) result = false
        when (user.diet) {
            "Vegano" -> if (product.ingredients_analysis?.nonVegan != null || product.ingredients_analysis?.maybeVegan != null ||
                product.traces_tags?.any { it in listOf("en:eggs", "en:molluscs", "en:crustaceans", "en:fish", "en:milk") } == true ||
                product.allergens_tags?.any { it in listOf("en:eggs", "en:molluscs", "en:crustaceans", "en:fish", "en:milk") } == true) result = false

            "Vegetariano" -> if (product.ingredients_analysis?.nonVegetarian != null || product.ingredients_analysis?.maybeVegetarian != null ||
                product.traces_tags?.any { it in listOf("en:molluscs", "en:crustaceans", "en:fish") } == true ||
                product.allergens_tags?.any { it in listOf("en:molluscs", "en:crustaceans", "en:fish") } == true) result = false
        }
        if (product.ecoscore_grade != null && product.ecoscore_grade.length == 1 && product.ecoscore_grade > user.ecoscore) result = false
        if (product.nutriscore_grade != null && product.nutriscore_grade.length == 1 && product.nutriscore_grade > user.nutriscore) result = false
        if (product.nova_group != null && product.nova_group in 1..4 && product.nova_group > user.novascore.toInt()) result = false
        if (user.otherDiets.contains("aceite de palma") && product.ingredients_analysis?.palmOil != null) result = false
        if (user.otherDiets.contains("carne de ternera")) {
            if(!product.allergens_tags.isNullOrEmpty() && product.allergens_tags.contains("en:beef"))result = false
            if(!product.ingredients.isNullOrEmpty() && containsBeef(product.ingredients)) result = false
            if(!product.traces_tags.isNullOrEmpty() && product.traces_tags.contains("en:beef")) result = false
        }
        if (user.otherDiets.contains("carne de cerdo")) {
            if (!product.allergens_tags.isNullOrEmpty() && product.allergens_tags.contains("en:pork")) result = false
            if(!product.ingredients.isNullOrEmpty() && containsPork(product.ingredients)) result = false
            if(!product.traces_tags.isNullOrEmpty() && product.traces_tags.contains("en:pork")) result = false
        }
        val weight = mapOf(
            "low" to 1,
            "moderate" to 2,
            "high" to 3
        )
        if (user.nutriments.isNotEmpty()) {
            if (product.nutrient_levels?.fat != null && weight[product.nutrient_levels.fat]!! > weight[user.nutriments["fats"]]!!) result = false
            if (product.nutrient_levels?.`saturated-fat` != null && weight[product.nutrient_levels.`saturated-fat`]!! > weight[user.nutriments["saturated-fats"]]!!) result = false
            if (product.nutrient_levels?.sugars != null && weight[product.nutrient_levels.sugars]!! > weight[user.nutriments["sugars"]]!!) result = false
            if (product.nutrient_levels?.salt != null && weight[product.nutrient_levels.salt]!! > weight[user.nutriments["salt"]]!!) result = false
        }
        return result
    }

    private fun containsPork(ingredients: List<Ingredient>): Boolean {
        for (ingredient in ingredients) {
            val termsToCheck = listOf("pork", "cerdo")
            val matches = termsToCheck.any { term ->
                ingredient.id?.contains(term, ignoreCase = true) == true ||
                ingredient.text?.contains(term, ignoreCase = true) == true
            }

            if (matches) {
                return true
            }

            ingredient.ingredients?.let {
                if (containsPork(it)) {
                    return true
                }
            }
        }
        return false
    }

    private fun containsBeef(ingredients: List<Ingredient>): Boolean {
        for (ingredient in ingredients) {
            val termsToCheck = listOf("beef", "veal", "ternera")
            val matches = termsToCheck.any { term ->
                ingredient.id?.contains(term, ignoreCase = true) == true ||
                ingredient.text?.contains(term, ignoreCase = true) == true
            }

            if (matches) {
                return true
            }

            ingredient.ingredients?.let {
                if (containsBeef(it)) {
                    return true
                }
            }
        }
        return false
    }

    private fun createIngredientsText(ingredientes: List<Ingredient>, ingredientsText: StringBuilder) {

        val dao = database.IngredientDao()

        for (ingredient in ingredientes) {

                ingredientsText.append(ingredient.id?.let {
                    dao.getNameByIdAndLanguage(it, "es")?.name
                } ?: ingredient.text ?: "")

            if (!ingredient.ingredients.isNullOrEmpty()) {
                ingredientsText.append(" (")

                createIngredientsText(ingredient.ingredients, ingredientsText)

                ingredientsText.setLength(ingredientsText.length - 2)
                ingredientsText.append(")")

            }

                ingredientsText.append(", ")

        }
    }

    private fun getColor(text: String): Int {
        return when (text) {
            "low" -> R.color.nutriments_low
            "moderate" -> R.color.nutriments_moderate
            "high" -> R.color.nutriments_high
            else -> R.color.almost_white
        }
    }

}