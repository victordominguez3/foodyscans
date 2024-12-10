package com.example.foodyscans.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodyscans.R
import com.example.foodyscans.databinding.SavetolistItemBinding
import com.example.foodyscans.models.ProductList

class SaveToListAdapter(private var listItem: MutableList<ProductList>, private val checkedItems: MutableList<ProductList>) : RecyclerView.Adapter<SaveToListAdapter.ViewHolder>() {

    private var listener: OnItemCheckedListener? = null

    interface OnItemCheckedListener {
        fun onItemChecked(item: ProductList)
        fun onItemUnchecked(item: ProductList)
    }

    fun setListener(listener: OnItemCheckedListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.savetolist_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]
        holder.bind(item)
    }

    fun setSaveLists(productLists: MutableList<ProductList>) {
        listItem = productLists
        notifyDataSetChanged()
    }

    inner class ViewHolder (view: View): RecyclerView.ViewHolder(view) {

        private val binding = SavetolistItemBinding.bind(view)

        @SuppressLint("SetTextI18n")
        fun bind(item: ProductList) {

            binding.listTitle.text = item.name
            binding.checkbox.isChecked = checkedItems.contains(item)

            binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (binding.checkbox.isChecked) {
                    listener?.onItemChecked(item)
                } else {
                    listener?.onItemUnchecked(item)
                }
            }

        }

    }

}