package com.example.foodyscans.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodyscans.R
import com.example.foodyscans.adapters.RecipeListAdapter
import com.example.foodyscans.adapters.RecipeListListener
import com.example.foodyscans.adapters.SaveListAdapter
import com.example.foodyscans.adapters.SaveListListener
import com.example.foodyscans.databinding.FragmentRecipesBinding
import com.example.foodyscans.dialogs.ChatRecipeFragment
import com.example.foodyscans.dialogs.CreateRecipeFragment
import com.example.foodyscans.dialogs.CreateRecipeListFragment
import com.example.foodyscans.dialogs.CreateSaveListFragment
import com.example.foodyscans.dialogs.GenerateRecipeFragment
import com.example.foodyscans.dialogs.LogOutFragment
import com.example.foodyscans.dialogs.RecipeListFragment
import com.example.foodyscans.dialogs.SaveListFragment
import com.example.foodyscans.dialogs.SettingsFragment
import com.example.foodyscans.models.Product
import com.example.foodyscans.models.Recipe
import com.example.foodyscans.models.ProductList
import com.example.foodyscans.models.RecipeList
import com.example.foodyscans.models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class RecipesFragment : Fragment(), RecipeListListener {

    private lateinit var binding: FragmentRecipesBinding
    private val userViewModel: UserViewModel by activityViewModels()

    private val chatRecipes: MutableList<Recipe> = mutableListOf()
    private val myRecipes: MutableList<Recipe> = mutableListOf()

    private lateinit var mListAdapter: RecipeListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycler()

        userViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            mListAdapter.setRecipeLists(user.recipes.sortedBy { it.name.lowercase() }.filter { it.name != "Generadas por ChatGPT" && it.name != "Mis recetas" }.toMutableList())
            binding.countChatText.text = "${user.recipes.find { it.name == "Generadas por ChatGPT" }?.recipes?.count()}"
            binding.countMyText.text = "${user.recipes.find { it.name == "Mis recetas" }?.recipes?.count()}"
            chatRecipes.clear()
            chatRecipes.addAll(user.recipes.find { it.name == "Generadas por ChatGPT" }?.recipes ?: emptyList())
            myRecipes.clear()
            myRecipes.addAll(user.recipes.find { it.name == "Mis recetas" }?.recipes ?: emptyList())
        })

        binding.chatList.setOnClickListener {
            if (chatRecipes.isNotEmpty()) {
                val dialogFragment = RecipeListFragment(RecipeList("Generadas por ChatGPT", chatRecipes))
                dialogFragment.show(parentFragmentManager, "")
            } else Toast.makeText(requireContext(), "No hay recetas", Toast.LENGTH_SHORT).show()
        }

        binding.myList.setOnClickListener {
            if (myRecipes.isNotEmpty()) {
                val dialogFragment = RecipeListFragment(RecipeList("Mis recetas", myRecipes))
                dialogFragment.show(parentFragmentManager, "")
            } else Toast.makeText(requireContext(), "No hay recetas", Toast.LENGTH_SHORT).show()
        }

        binding.newItem.setOnClickListener {
            showMenu(it)
        }

    }

    private fun setRecycler() {

        mListAdapter = RecipeListAdapter(mutableListOf(), this)
        mLayoutManager = LinearLayoutManager(requireContext())

        binding.listsRecycler.apply {
            layoutManager = mLayoutManager
            adapter = mListAdapter
        }

    }

    override fun onClick(recipeList: RecipeList) {
        if (recipeList.recipes.isNotEmpty()) {
            val dialogFragment = RecipeListFragment(recipeList)
            dialogFragment.show(parentFragmentManager, "")
        } else Toast.makeText(requireContext(), "No hay recetas", Toast.LENGTH_SHORT).show()
    }

    override fun onLongClick(recipeList: RecipeList) {

        val builder = AlertDialog.Builder(requireContext())
            .setItems(
                arrayOf("Editar", "Eliminar")
            ) { dialog, which ->
                if (which == 0) {

                    val dialogFragment = CreateRecipeListFragment()
                    val bundle = Bundle()
                    bundle.putString("name", recipeList.name)
                    dialogFragment.arguments = bundle
                    dialogFragment.show(parentFragmentManager, "edit")

                } else {

                    if (recipeList.recipes.isEmpty()) {

                        val builder = AlertDialog.Builder(requireContext())

                        builder.setTitle("Alerta").setMessage("¿Está seguro de que desea borrar la lista ${recipeList.name}?")

                        builder.setPositiveButton("Sí") { _, _ ->
                            userViewModel.deleteRecipeList(recipeList) { success ->
                                if (!success) {
                                    Toast.makeText(requireContext(), "Ocurrió un error al eliminar la lista", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        builder.setNegativeButton("No") { _, _ ->

                        }

                        val dialog = builder.create()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_shape)
                        dialog.show()

                    } else {

                        val builder = AlertDialog.Builder(requireContext())

                        builder.setTitle("Alerta")
                            .setMessage("La lista contiene recetas. ¿Está seguro de que desea borrar la lista?")

                        builder.setPositiveButton("Sí") { _, _ ->
                            userViewModel.deleteRecipeList(recipeList) { success ->
                                if (!success) {
                                    Toast.makeText(requireContext(), "Ocurrió un error al eliminar la lista", Toast.LENGTH_SHORT).show()
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
            }
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_shape)
        dialog.show()
    }

    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.popup_recipes, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_list -> {

                    val dialogFragment = CreateRecipeListFragment()
                    dialogFragment.show(parentFragmentManager, "new")

                    true
                }
                R.id.new_recipe -> {

                    val dialogFragment = CreateRecipeFragment()
                    dialogFragment.show(parentFragmentManager, "new")

                    true
                }
                R.id.generate_recipe -> {

                    val dialogFragment = GenerateRecipeFragment()
                    dialogFragment.show(parentFragmentManager, "")

                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    fun scrollToTop() {
        binding.scroll.smoothScrollTo(0, 0)
    }

}