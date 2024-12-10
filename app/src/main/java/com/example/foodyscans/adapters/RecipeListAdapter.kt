package com.example.foodyscans.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.foodyscans.R
import com.example.foodyscans.databinding.RecipelistItemBinding
import com.example.foodyscans.models.RecipeList

class RecipeListAdapter(private var listItem: MutableList<RecipeList>, private var listener: RecipeListListener) : RecyclerView.Adapter<RecipeListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipelist_item, parent, false)
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

    fun setRecipeLists(recipeLists: MutableList<RecipeList>) {
        listItem = recipeLists
        notifyDataSetChanged()
    }

    fun add(recipeList: RecipeList?) {
        if (recipeList != null) {
            listItem.add(recipeList)
            listItem.sortBy { it.name.lowercase() }
            notifyDataSetChanged()
        }
    }

    fun modify(updatedRecipeList: RecipeList?, oldName: String?) {
        if (updatedRecipeList != null && oldName != null) {
            val index = listItem.indexOfFirst { it.name == oldName }
            if (index != -1) {
                listItem[index] = updatedRecipeList
                notifyDataSetChanged()
            }
        }
    }

    fun delete(recipeList: RecipeList) {
        val index = listItem.indexOf(recipeList)
        if (index != -1){
            listItem.removeAt(index)
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder (view: View): RecyclerView.ViewHolder(view) {

        private val binding = RecipelistItemBinding.bind(view)

        @SuppressLint("SetTextI18n")
        fun bind(item: RecipeList) {

            binding.listTitle.text = item.name
            binding.productCountText.text = "${item.recipes.count()}"

        }

        fun setListener(recipeList: RecipeList) {
            binding.root.setOnClickListener {
                listener.onClick(recipeList)
            }
            binding.root.setOnLongClickListener {
                listener.onLongClick(recipeList)
                true
            }
        }

    }

}