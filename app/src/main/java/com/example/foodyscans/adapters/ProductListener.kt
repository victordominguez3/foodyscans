package com.example.foodyscans.adapters

import com.example.foodyscans.models.Product

interface ProductListener {
    fun onClick(product: Product)
    fun onLongClick(product: Product)
}