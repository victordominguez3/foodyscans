package com.example.foodyscans.dialogs

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodyscans.R
import com.example.foodyscans.adapters.RecipeAdapter
import com.example.foodyscans.adapters.RecipeListener
import com.example.foodyscans.databinding.FragmentRecipeListBinding
import com.example.foodyscans.models.Recipe
import com.example.foodyscans.models.RecipeList
import com.example.foodyscans.models.User
import com.example.foodyscans.models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class RecipeListFragment(private val recipeList: RecipeList) : DialogFragment(), RecipeListener {

    private lateinit var binding: FragmentRecipeListBinding
    private val userViewModel: UserViewModel by activityViewModels()

    private var user = User()

    private lateinit var mListAdapter: RecipeAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

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
        binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            this.user = user
            setRecycler()
        })

        binding.title.text = recipeList.name
    }

    private fun setRecycler() {

        mListAdapter = RecipeAdapter(user.recipes.find { it.name == recipeList.name }?.recipes ?: mutableListOf(), this)
        mLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        binding.recipesRecycler.apply {
            layoutManager = mLayoutManager
            adapter = mListAdapter
        }
    }

    override fun onClick(recipe: Recipe) {
        val dialogFragment = RecipeFragment(recipe)
        dialogFragment.show(parentFragmentManager, "")
    }

    override fun onLongClick(recipe: Recipe) {

        var items = if (recipeList.name == "Generadas por ChatGPT") arrayOf("Eliminar") else arrayOf("Editar", "Eliminar")

        val builder = AlertDialog.Builder(requireContext())
            .setItems(
                items
            ) { dialog, which ->

                val selectedOption = items[which]

                if (selectedOption == "Editar") {

                    val dialogFragment = CreateRecipeFragment()
                    val bundle = Bundle()
                    bundle.putString("id", recipe.id.toString())
                    bundle.putString("list", recipeList.name)
                    dialogFragment.arguments = bundle
                    dialogFragment.show(parentFragmentManager, "edit")

                } else {

                    val builder = AlertDialog.Builder(requireContext())

                    builder.setTitle("Alerta").setMessage("¿Eliminar ${recipe.title}?")

                    builder.setPositiveButton("Sí") { _, _ ->
                        userViewModel.removeRecipeFromList(recipe, recipeList) { success ->
                            if (!success) {
                                Toast.makeText(requireContext(), "Error al eliminar la receta", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    builder.setNegativeButton("No") { _, _ ->

                    }

                    val dialog = builder.create()
                    dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_shape)
                    dialog.show()

                }
            }
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_shape)
        dialog.show()
    }
}