package com.example.foodyscans.dialogs

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.foodyscans.R
import com.example.foodyscans.databinding.FragmentCreateRecipeListBinding
import com.example.foodyscans.databinding.FragmentCreateSaveListBinding
import com.example.foodyscans.models.Ingredient
import com.example.foodyscans.models.NutrientLevels
import com.example.foodyscans.models.Product
import com.example.foodyscans.models.ProductList
import com.example.foodyscans.models.Recipe
import com.example.foodyscans.models.RecipeList
import com.example.foodyscans.models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class CreateRecipeListFragment : DialogFragment() {

    private lateinit var binding: FragmentCreateRecipeListBinding
    private val userViewModel: UserViewModel by activityViewModels()

    private var keyList = listOf<String>()

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateRecipeListBinding.inflate(inflater, container, false)

        if (tag == "edit") {
            binding.title.text = "Editar lista de recetas"
            binding.name.setText(arguments?.getString("name"))
        } else if (tag == "new") {
            binding.title.text = "Nueva lista de recetas"
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            keyList = user.recipes.map { it.name }
        })

        binding.cancelButton.setOnClickListener {
            this.dismiss()
        }

        binding.confirmButton.setOnClickListener {

            if (binding.name.text.isNullOrEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "El campo nombre es obligatorio",
                    Toast.LENGTH_SHORT
                ).show()

            } else if(tag == "new" && keyList.contains(binding.name.text.toString())) {

                Toast.makeText(requireContext(), "Ya existe una lista con el mismo nombre", Toast.LENGTH_SHORT).show()

            } else if(tag == "edit" && keyList.contains(binding.name.text.toString()) && binding.name.text.toString() != arguments?.getString("name")) {

                Toast.makeText(requireContext(), "Ya existe una lista con el mismo nombre", Toast.LENGTH_SHORT).show()

            } else {

                if (tag == "edit") {

                    userViewModel.editRecipeList(
                        initName = arguments?.getString("name")!!,
                        name = binding.name.text.toString(),
                    ) { success ->
                        if (success) {
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Ocurrió un error al editar la lista", Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {

                    userViewModel.createRecipeList(binding.name.text.toString()) { success ->
                        if (success) {
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Ocurrió un error al crear la lista", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

}