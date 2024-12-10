package com.example.foodyscans.dialogs

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodyscans.adapters.IngredientStepAdapter
import com.example.foodyscans.api.OpenAIClient
import com.example.foodyscans.databinding.FragmentGenerateRecipeBinding
import com.example.foodyscans.models.ChatRequest
import com.example.foodyscans.models.ChatResponse
import com.example.foodyscans.models.Message
import com.example.foodyscans.models.Recipe
import com.example.foodyscans.models.RecipeContent
import com.example.foodyscans.models.User
import com.example.foodyscans.models.UserViewModel
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class GenerateRecipeFragment : DialogFragment(), IngredientStepAdapter.OnItemDeleteListener {

    private lateinit var binding: FragmentGenerateRecipeBinding
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var mListAdapter: IngredientStepAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    private val ingredients = mutableListOf<String>()

    private var user = User()

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
        binding = FragmentGenerateRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            this.user = user
            setRecycler()
        })

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        binding.tab1.visibility = View.VISIBLE
                        binding.tab2.visibility = View.GONE
                    }
                    1 -> {
                        binding.tab1.visibility = View.GONE
                        binding.tab2.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

        setRecycler()

        binding.ingredientText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val ingredient = binding.ingredientText.text.toString().trim().replaceFirstChar { it.uppercase() }
                if (ingredient.isNotEmpty()) {
                    binding.ingredientsRecycler.visibility = View.VISIBLE
                    mListAdapter.add(ingredient)
                    ingredients.add(ingredient)
                    binding.ingredientText.text?.clear()
                }
                true
            } else false
        }

        binding.generateIngredientsButton.setOnClickListener {

            val prompt = "Dame una receta con los ingredientes: ${ingredients.joinToString(", ")}. La respuesta debe ser exclusivamente un JSON con los campos: titulo (String), ingredientes (array), instrucciones (array). Ningún comentario que no sea el JSON. Las intrucciones no las quiero numeradas. Si algun ingrediente no existe omite ese ingrediente y utiliza los que si son validos. Si ninguno de los ingredientes existen devuelve todos los campos vacios."

            val message = Message(role = "user", content = prompt)
            val request = ChatRequest(model = "gpt-3.5-turbo", messages = listOf(message))

            OpenAIClient.openAIService.sendPrompt(request).enqueue(object : Callback<ChatResponse> {
                override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                    if (response.isSuccessful) {
                        val chatResponse = response.body()

                        val json = chatResponse?.choices?.get(0)?.message?.content

                        val jsonRecipe = Gson().fromJson(json, RecipeContent::class.java)

                        val recipe = Recipe(
                            UUID.randomUUID(),
                            jsonRecipe.titulo,
                            jsonRecipe.ingredientes,
                            jsonRecipe.instrucciones
                        )

                        val dialogFragment = ChatRecipeFragment(recipe)
                        dialogFragment.show(parentFragmentManager, "")

                    } else {
                        Log.e("ChatGPT", "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                    Log.e("ChatGPT", "Fallo en la solicitud: ${t.message}")
                }
            })

        }

        binding.generateNameButton.setOnClickListener {

            val prompt = "Dame una receta para este plato: ${binding.nameText.text}. La respuesta debe ser exclusivamente un JSON con los campos: titulo (String), ingredientes (array), instrucciones (array). Ningún comentario que no sea el JSON. Las intrucciones no las quiero numeradas. Si el plato no existe devuelve todos los campos vacios."

            val message = Message(role = "user", content = prompt)
            val request = ChatRequest(model = "gpt-3.5-turbo", messages = listOf(message))

            OpenAIClient.openAIService.sendPrompt(request).enqueue(object : Callback<ChatResponse> {
                override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                    if (response.isSuccessful) {
                        val chatResponse = response.body()

                        val json = chatResponse?.choices?.get(0)?.message?.content

                        val jsonRecipe = Gson().fromJson(json, RecipeContent::class.java)

                        val recipe = Recipe(
                            UUID.randomUUID(),
                            jsonRecipe.titulo,
                            jsonRecipe.ingredientes,
                            jsonRecipe.instrucciones
                        )

                        val dialogFragment = ChatRecipeFragment(recipe)
                        dialogFragment.show(parentFragmentManager, "")

                    } else {
                        Log.e("ChatGPT", "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                    Log.e("ChatGPT", "Fallo en la solicitud: ${t.message}")
                }
            })

        }

    }

    private fun setRecycler() {

        mListAdapter = IngredientStepAdapter(mutableListOf(), "ingredient")
        mLayoutManager = LinearLayoutManager(requireContext())
        mListAdapter.setListener(this)

        binding.ingredientsRecycler.apply {
            layoutManager = mLayoutManager
            adapter = mListAdapter
        }

    }

    override fun onItemDelete(item: String, type: String) {
        mListAdapter.delete(item)
        ingredients.remove(item)
        if (mListAdapter.itemCount == 0) binding.ingredientsRecycler.visibility = View.GONE
    }

}