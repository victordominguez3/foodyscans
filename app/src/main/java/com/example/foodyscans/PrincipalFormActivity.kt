package com.example.foodyscans

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.foodyscans.databinding.ActivityPrincipalFormBinding
import com.example.foodyscans.databinding.ActivityRegisterBinding
import com.example.foodyscans.fragments.AllergensFragment
import com.example.foodyscans.fragments.DietsFragment
import com.example.foodyscans.fragments.MarkersFragment
import com.example.foodyscans.fragments.ProfileCardFragment
import com.example.foodyscans.fragments.RecipesFragment
import com.example.foodyscans.fragments.ScoresFragment
import com.example.foodyscans.models.FormViewModel
import java.text.Normalizer.Form

class PrincipalFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPrincipalFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.almost_white)

        setFragment(1)
    }

    fun setFragment(num: Int) {
        val fragment = when (num) {
            1 -> ScoresFragment()
            2 -> AllergensFragment()
            3 -> DietsFragment()
            4 -> ProfileCardFragment()
            else -> throw IllegalArgumentException("Fragmento no válido")
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment, fragment)
        fragmentTransaction.commit()

        val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
        windowInsetsController?.isAppearanceLightStatusBars = true
    }

    fun finishForm(isInitialForm: Boolean, data: FormViewModel) {
        if (isInitialForm) {
            intent = Intent(this, AppActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent()
            intent.putExtra("nutriscore", data.nutriscore)
            intent.putExtra("novascore", data.novascore)
            intent.putExtra("ecoscore", data.ecoscore)
            intent.putStringArrayListExtra("allergens", ArrayList(data.allergens))
            intent.putExtra("diet", data.diet)
            intent.putStringArrayListExtra("otherDiets", ArrayList(data.otherDiets))
            intent.putExtra("nutriments", HashMap(data.nutriments))
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}