package com.example.foodyscans.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodyscans.R
import com.example.foodyscans.databinding.SavelistItemBinding
import com.example.foodyscans.models.ProductList

class SaveListAdapter(private var listItem: MutableList<ProductList>, private var listener: SaveListListener) : RecyclerView.Adapter<SaveListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.savelist_item, parent, false)
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

    fun setSaveLists(productLists: MutableList<ProductList>) {
        listItem = productLists
        notifyDataSetChanged()
    }

    fun add(productList: ProductList?) {
        if (productList != null) {
            listItem.add(productList)
            listItem.sortBy { it.name.lowercase() }
            notifyDataSetChanged()
        }
    }

    fun modify(updatedProductList: ProductList?, oldName: String?) {
        if (updatedProductList != null && oldName != null) {
            val index = listItem.indexOfFirst { it.name == oldName }
            if (index != -1) {
                listItem[index] = updatedProductList
                notifyDataSetChanged()
            }
        }
    }

    fun delete(productList: ProductList) {
        val index = listItem.indexOf(productList)
        if (index != -1){
            listItem.removeAt(index)
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder (view: View): RecyclerView.ViewHolder(view) {

        private val binding = SavelistItemBinding.bind(view)

        @SuppressLint("SetTextI18n")
        fun bind(item: ProductList) {

            binding.listTitle.text = item.name
            binding.productCountText.text = "${item.products.count()}"

        }

        fun setListener(productList: ProductList) {
            binding.root.setOnClickListener {
                listener.onClick(productList)
            }
            binding.root.setOnLongClickListener {
                listener.onLongClick(productList)
                true
            }
        }

    }

}