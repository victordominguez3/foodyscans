package com.example.foodyscans.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.foodyscans.databinding.FragmentChatRecipeBinding
import com.example.foodyscans.models.Recipe
import com.example.foodyscans.models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ChatRecipeFragment(private val recipe: Recipe) : DialogFragment() {

    private lateinit var binding: FragmentChatRecipeBinding
    private val userViewModel: UserViewModel by activityViewModels()

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
        binding = FragmentChatRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.title.text = recipe.title ?: "Sin nombre"
        binding.ingredients.text = recipe.ingredients?.joinToString(separator = "\n") { "\u2022   $it" }
        binding.steps.text = recipe.steps?.mapIndexed { index, item -> "${index + 1} \u2192   $item" }?.joinToString(separator = "\n")

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {

            userViewModel.saveChatRecipe(recipe) { success ->
                if (success) {
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Error al guardar la receta", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

}