package com.example.foodyscans.dialogs

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodyscans.adapters.IngredientStepAdapter
import com.example.foodyscans.databinding.FragmentCreateRecipeBinding
import com.example.foodyscans.models.UserViewModel

class CreateRecipeFragment : DialogFragment(), IngredientStepAdapter.OnItemDeleteListener {

    private lateinit var binding: FragmentCreateRecipeBinding
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var mIngredientsListAdapter: IngredientStepAdapter
    private lateinit var mStepsListAdapter: IngredientStepAdapter
    private lateinit var mIngredientsLayoutManager: LinearLayoutManager
    private lateinit var mStepsLayoutManager: LinearLayoutManager

    private val ingredientsList: MutableList<String> = mutableListOf()
    private val stepsList: MutableList<String> = mutableListOf()

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclers()

        userViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            val options = user.recipes.map { it.name }.sorted().toMutableList()
            options.removeAll(arrayOf("Generadas por ChatGPT"))
            if (options.isEmpty()) {
                options.add("No hay listas disponibles")
            }
            val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, options.toTypedArray())
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            binding.listSpinner.adapter = adapter
            binding.listSpinner.setSelection(options.indexOf("Mis recetas"))
            if (tag == "edit") {
                binding.title.text = "Editar receta"
                binding.listSpinner.setSelection(options.indexOf(arguments?.getString("list")))
                val recipe = user.recipes.find { it.name == arguments?.getString("list") }?.recipes?.find { it.id.toString() == arguments?.getString("id") }
                if (recipe != null) {
                    binding.nameText.setText(recipe.title)
                    for (ingredient in recipe.ingredients!!) ingredientsList.add(ingredient)
                    mIngredientsListAdapter.notifyDataSetChanged()
                    for (step in recipe.steps!!) stepsList.add(step)
                    mStepsListAdapter.notifyDataSetChanged()
                }
            }
        })

        binding.newItem.setOnClickListener {
            val dialogFragment = CreateRecipeListFragment()
            dialogFragment.show(parentFragmentManager, "new")
        }

        binding.ingredientText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val ingredient = binding.ingredientText.text.toString().trim().replaceFirstChar { it.uppercase() }
                if (ingredient.isNotEmpty()) {
                    mIngredientsListAdapter.add(ingredient)
                    binding.ingredientText.text?.clear()
                }
                true
            } else false
        }

        binding.stepText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val step = binding.stepText.text.toString().trim().replaceFirstChar { it.uppercase() }
                if (step.isNotEmpty()) {
                    mStepsListAdapter.add(step)
                    binding.stepText.text?.clear()
                }
                true
            } else false
        }

        binding.cancelButton.setOnClickListener {
            this.dismiss()
        }

        binding.confirmButton.setOnClickListener {

            if (binding.nameText.text.isNullOrEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "El campo nombre es obligatorio",
                    Toast.LENGTH_SHORT
                ).show()

            } else if(ingredientsList.isEmpty()) {

                Toast.makeText(requireContext(), "Debe haber al menos un ingrediente", Toast.LENGTH_SHORT).show()

            } else if(stepsList.isEmpty()) {

                Toast.makeText(requireContext(), "Debe haber al menos un paso", Toast.LENGTH_SHORT).show()

            } else {

                if (tag == "edit") {

                    userViewModel.editRecipe(
                        initList = arguments?.getString("list")!!,
                        selectedList = binding.listSpinner.selectedItem.toString(),
                        recipeId = arguments?.getString("id")!!,
                        name = binding.nameText.text.toString(),
                        ingredientsList = ingredientsList,
                        stepsList = stepsList
                    ) { success ->
                        if (success) {
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Error al editar la receta", Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {

                    userViewModel.createRecipe(
                        name = binding.nameText.text.toString(),
                        ingredientsList = ingredientsList,
                        stepsList = stepsList,
                        selectedList = binding.listSpinner.selectedItem.toString()
                    ) { success ->
                        if (success) {
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Error al crear la receta", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun setRecyclers() {

        mIngredientsListAdapter = IngredientStepAdapter(ingredientsList, "ingredient")
        mStepsListAdapter = IngredientStepAdapter(stepsList, "step")
        mIngredientsListAdapter.setListener(this)
        mStepsListAdapter.setListener(this)
        mIngredientsLayoutManager = LinearLayoutManager(requireContext())
        mStepsLayoutManager = LinearLayoutManager(requireContext())

        binding.ingredientsRecycler.apply {
            layoutManager = mIngredientsLayoutManager
            adapter = mIngredientsListAdapter
        }
        binding.stepsRecycler.apply {
            layoutManager = mStepsLayoutManager
            adapter = mStepsListAdapter
        }

    }

    override fun onItemDelete(item: String, type: String) {
        if (type == "ingredient") {
            mIngredientsListAdapter.delete(item)
        } else {
            mStepsListAdapter.delete(item)
        }
    }

}