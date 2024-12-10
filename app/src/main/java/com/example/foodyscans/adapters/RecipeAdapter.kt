package com.example.foodyscans.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodyscans.R
import com.example.foodyscans.databinding.RecipeItemBinding
import com.example.foodyscans.models.Recipe

class RecipeAdapter(private var listItem: MutableList<Recipe>, private var listener: RecipeListener) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
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

    fun setRecipes(recipes: MutableList<Recipe>) {
        listItem = recipes
        notifyDataSetChanged()
    }

    fun add(recipe: Recipe?) {
        if (recipe != null) {
            listItem.add(recipe)
            notifyDataSetChanged()
        }
    }

    fun modify(updatedRecipe: Recipe?) {
        if (updatedRecipe != null) {
            val index = listItem.indexOfFirst { it.id == updatedRecipe.id }
            if (index != -1) {
                listItem[index] = updatedRecipe
                notifyDataSetChanged()
            }
        }
    }

    fun delete(recipe: Recipe) {
        val index = listItem.indexOf(recipe)
        if (index != -1){
            listItem.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inner class ViewHolder (view: View): RecyclerView.ViewHolder(view) {

        private val binding = RecipeItemBinding.bind(view)

        @SuppressLint("SetTextI18n")
        fun bind(item: Recipe) {

            binding.title.text = item.title

        }

        fun setListener(recipe: Recipe) {
            binding.root.setOnClickListener {
                listener.onClick(recipe)
            }
            binding.root.setOnLongClickListener {
                listener.onLongClick(recipe)
                true
            }
        }

    }

}