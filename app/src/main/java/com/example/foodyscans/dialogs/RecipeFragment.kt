package com.example.foodyscans.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.example.foodyscans.databinding.FragmentRecipeBinding
import com.example.foodyscans.models.Recipe
import java.io.File
import java.io.FileOutputStream

class RecipeFragment(private val recipe: Recipe) : DialogFragment() {

    private lateinit var binding: FragmentRecipeBinding

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.title.text = recipe.title ?: "Sin nombre"
        binding.ingredients.text = recipe.ingredients?.joinToString(separator = "\n") { "\u2022   $it" }
        binding.steps.text = recipe.steps?.mapIndexed { index, item -> "${index + 1} \u2192   $item" }?.joinToString(separator = "\n")

    }

}