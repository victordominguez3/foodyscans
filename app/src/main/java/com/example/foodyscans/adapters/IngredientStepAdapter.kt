package com.example.foodyscans.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodyscans.R
import com.example.foodyscans.databinding.IngredientStepItemBinding

class IngredientStepAdapter(private var listItem: MutableList<String>, private var type: String) : RecyclerView.Adapter<IngredientStepAdapter.ViewHolder>() {

    private var listener: OnItemDeleteListener? = null

    interface OnItemDeleteListener {
        fun onItemDelete(item: String, type: String)
    }

    fun setListener(listener: OnItemDeleteListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_step_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]
        holder.bind(item)
    }

    fun setItems(items: MutableList<String>) {
        listItem = items
        notifyDataSetChanged()
    }

    fun add(item: String) {
        listItem.add(item)
        notifyDataSetChanged()
    }

    fun delete(item: String) {
        val index = listItem.indexOf(item)
        if (index != -1){
            listItem.removeAt(index)
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder (view: View): RecyclerView.ViewHolder(view) {

        private val binding = IngredientStepItemBinding.bind(view)

        @SuppressLint("SetTextI18n")
        fun bind(item: String) {

            binding.text.text = item

            binding.delete.setOnClickListener {
                listener?.onItemDelete(item, type)
            }

        }

    }

}