package com.example.foodyscans.adapters

import com.example.foodyscans.models.ProductList

interface SaveListListener {
    fun onClick(productList: ProductList)
    fun onLongClick(productList: ProductList)
}