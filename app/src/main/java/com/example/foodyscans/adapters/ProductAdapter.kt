package com.example.foodyscans.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.foodyscans.R
import com.example.foodyscans.databinding.ProductItemBinding
import com.example.foodyscans.models.Ingredient
import com.example.foodyscans.models.Product
import com.example.foodyscans.models.User

class ProductAdapter(private var listItem: MutableList<Product>, private var listener: ProductListener, private val user: User) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]
        holder.bind(item)
        holder.setListener(item)
    }

    fun setProducts(products: MutableList<Product>) {
        listItem = products
        notifyDataSetChanged()
    }

    fun addProducts(products: MutableList<Product>) {
        listItem.addAll(products)
        notifyDataSetChanged()
    }

    fun delete(product: Product) {
        val index = listItem.indexOf(product)
        if (index != -1){
            listItem.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inner class ViewHolder (view: View): RecyclerView.ViewHolder(view) {

        private val binding = ProductItemBinding.bind(view)

        @SuppressLint("SetTextI18n")
        fun bind(item: Product) {

            try {
                Glide.with(binding.image)
                    .load(item.image_url)
                    .transform(CenterCrop(), RoundedCorners(20))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.image)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            binding.name.text = item.product_name
            binding.brand.text = item.brands
            binding.quantity.text = item.quantity

            val aptColor = if (isApt(item)) {
                ContextCompat.getColor(binding.root.context, R.color.dark_green_5)
            } else {
                ContextCompat.getColor(binding.root.context, R.color.nutriments_high)
            }
            binding.aptIndicator.setBackgroundColor(aptColor)
        }

        fun setListener(product: Product) {
            binding.root.setOnClickListener {
                listener.onClick(product)
            }
            binding.root.setOnLongClickListener {
                listener.onLongClick(product)
                true
            }
        }

    }

    private fun isApt(product: Product): Boolean {

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

}