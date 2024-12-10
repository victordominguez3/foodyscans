package com.example.foodyscans

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.foodyscans.database.IngredientsApplication.Companion.database
import com.example.foodyscans.databinding.ActivityAppBinding
import com.example.foodyscans.fragments.ExploreFragment
import com.example.foodyscans.fragments.MarkersFragment
import com.example.foodyscans.fragments.ProfileFragment
import com.example.foodyscans.fragments.RecipesFragment
import com.example.foodyscans.models.IngredientDb
import com.example.foodyscans.models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader

class AppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val fragments = mutableMapOf<Int, Fragment>()
    private var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE)

        if (!sharedPreferences.getBoolean("ingredients", false)) {
            database.IngredientDao().deleteAllIngredients()
            saveIngredientsFromJson(loadJsonFromAssets())
            Log.d("Ingredients", "Ingredients loaded from JSON")
        } else {
            Log.d("Ingredients", "Ingredients already loaded")
        }

        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_green_1)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        fragments[R.id.explore] = ExploreFragment()
        fragments[R.id.marker] = MarkersFragment()
        fragments[R.id.recipes] = RecipesFragment()
        fragments[R.id.profile] = ProfileFragment()

        if (savedInstanceState == null) {
            setFragment(R.id.explore)
        }

        binding.nav.selectedItemId = R.id.explore

        binding.nav.setOnItemSelectedListener {
            setFragment(it.itemId)
            true
        }

    }

    private fun setFragment(itemId: Int) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val selectedFragment = fragments[itemId]

        if (itemId == R.id.explore && activeFragment is ExploreFragment) {
            (activeFragment as? ExploreFragment)?.scrollToTop()
            return
        } else if (itemId == R.id.marker && activeFragment is MarkersFragment) {
            (activeFragment as? MarkersFragment)?.scrollToTop()
            return
        } else if (itemId == R.id.recipes && activeFragment is RecipesFragment) {
            (activeFragment as? RecipesFragment)?.scrollToTop()
            return
        } else if (itemId == R.id.profile && activeFragment is ProfileFragment) {
            (activeFragment as? ProfileFragment)?.scrollToTop()
            return
        } else {
            activeFragment?.let { fragmentTransaction.hide(it) }

            if (selectedFragment != null) {
                if (!selectedFragment.isAdded) {
                    fragmentTransaction.add(R.id.fragment, selectedFragment)
                }
                fragmentTransaction.show(selectedFragment).commit()
                activeFragment = selectedFragment
            }
        }

        when (selectedFragment) {
            is MarkersFragment, is RecipesFragment -> {
                window.statusBarColor = ContextCompat.getColor(this, R.color.almost_white)
                setStatusBarTextColor(true)
            }
            else -> {
                window.statusBarColor = ContextCompat.getColor(this, R.color.dark_green_1)
                setStatusBarTextColor(false)
            }
        }

        val isLightBackground = when (selectedFragment) {
            is MarkersFragment, is RecipesFragment -> true
            else -> false
        }
        setStatusBarTextColor(isLightBackground)
    }

    private fun setStatusBarTextColor(isLightBackground: Boolean) {
        val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
        windowInsetsController?.isAppearanceLightStatusBars = isLightBackground
    }

    private fun loadJsonFromAssets(): String {
        val inputStream = this.assets.open("ingredients.json")
        val reader = InputStreamReader(inputStream)
        return reader.readText().also { reader.close() }
    }

    private fun saveIngredientsFromJson(jsonString: String) {

        val ingredientDao = database.IngredientDao()

        val jsonObject = JsonParser.parseString(jsonString).asJsonObject

        val ingredientList = mutableListOf<IngredientDb>()

        for (key in jsonObject.keySet()) {
            val item = jsonObject[key].asJsonObject
            if (item.has("name")) {
                val namesObject = item.getAsJsonObject("name")
                for ((language, name) in namesObject.entrySet()) {
                    if (name != null && !name.isJsonNull) {
                        val ingredient = IngredientDb(
                            id = key,
                            language = language,
                            name = name.asString
                        )
                        ingredientList.add(ingredient)
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            Log.d("Ingredients", "Ingredients saving to database")
            ingredientDao.insertAll(ingredientList)
            Log.d("Ingredients", "Ingredients saved to database")
            sharedPreferences.edit().putBoolean("ingredients", true).apply()
        }
    }
}