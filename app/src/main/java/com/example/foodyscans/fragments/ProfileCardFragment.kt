package com.example.foodyscans.fragments

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.foodyscans.PrincipalFormActivity
import com.example.foodyscans.R
import com.example.foodyscans.databinding.FragmentDietsBinding
import com.example.foodyscans.databinding.FragmentProfileCardBinding
import com.example.foodyscans.models.FormViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileCardFragment : Fragment() {

    private lateinit var binding: FragmentProfileCardBinding
    private val formViewModel: FormViewModel by activityViewModels()
    private lateinit var sharedPreferences: SharedPreferences

    private val auth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileCardBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("config", MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (formViewModel.nutriscore) {
            "a" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_a)
            "b" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_b)
            "c" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_c)
            "d" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_d)
            "e" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_e)
        }
        when (formViewModel.novascore) {
            "1" -> binding.novaImage.setImageResource(R.drawable.nova_group_1)
            "2" -> binding.novaImage.setImageResource(R.drawable.nova_group_2)
            "3" -> binding.novaImage.setImageResource(R.drawable.nova_group_3)
            "4" -> binding.novaImage.setImageResource(R.drawable.nova_group_4)
        }
        when (formViewModel.ecoscore) {
            "a" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_a)
            "b" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_b)
            "c" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_c)
            "d" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_d)
            "e" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_e)
        }
        if (formViewModel.allergens.isNotEmpty()) {

            val text = if (formViewModel.allergens.size > 1) {
                val first = formViewModel.allergens.dropLast(1).joinToString(", ") { getName(it) }
                val last = getName(formViewModel.allergens.last())
                "${first.replaceFirstChar { it.uppercase() }} y $last"
            } else {
                formViewModel.allergens.joinToString { getName(it) }.replaceFirstChar { it.uppercase() }
            }
            binding.allergensText.text = text
        } else {
            binding.allergensText.text = "No"
        }
        if (formViewModel.diet != "no" || formViewModel.otherDiets.isNotEmpty()) {
            var text = ""
            if (formViewModel.diet != "no") {
                text = formViewModel.diet
            }
            if (formViewModel.otherDiets.isNotEmpty()) {
                text += if (formViewModel.diet != "no") "\nDieta sin " else "Dieta sin "
                when (formViewModel.otherDiets.count()) {
                    1 -> text += formViewModel.otherDiets[0]
                    2 -> text += formViewModel.otherDiets[0] + " ni " + formViewModel.otherDiets[1]
                    3 -> text += formViewModel.otherDiets[0] + ", " + formViewModel.otherDiets[1] + " ni " + formViewModel.otherDiets[2]
                }
            }
            binding.dietsText.text = text
        } else {
            binding.dietsText.text = "No"
        }

        binding.fatsText.text = mapOf("low" to "Bajo", "moderate" to "Moderado", "high" to "Alto").getOrDefault(formViewModel.nutriments["fats"], "Desconocido")
        binding.saturatedFatsText.text = mapOf("low" to "Bajo", "moderate" to "Moderado", "high" to "Alto").getOrDefault(formViewModel.nutriments["saturated-fats"], "Desconocido")
        binding.sugarsText.text = mapOf("low" to "Bajo", "moderate" to "Moderado", "high" to "Alto").getOrDefault(formViewModel.nutriments["sugars"], "Desconocido")
        binding.saltText.text = mapOf("low" to "Bajo", "moderate" to "Moderado", "high" to "Alto").getOrDefault(formViewModel.nutriments["salt"], "Desconocido")

        binding.backButton.setOnClickListener {
            (activity as PrincipalFormActivity).setFragment(3)
        }

        binding.finishButton.setOnClickListener {
            val newData = hashMapOf(
                "nutriscore" to formViewModel.nutriscore,
                "novascore" to formViewModel.novascore,
                "ecoscore" to formViewModel.ecoscore,
                "allergens" to formViewModel.allergens,
                "allergens_tags" to formViewModel.allergens.map { it.toAllergenTag() },
                "diet" to formViewModel.diet,
                "otherDiets" to formViewModel.otherDiets,
                "nutriments" to formViewModel.nutriments,
                "initialForm" to true
            )

            formViewModel.saveData(newData) { success ->
                if (success) {
                    sharedPreferences.edit().putBoolean("${auth.currentUser!!.uid}_initialForm", true).apply()
                    (activity as PrincipalFormActivity).finishForm(formViewModel.initialForm, formViewModel)
                } else {
                    Toast.makeText(requireContext(), "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getName(idName: String): String {
        return when (idName) {
            "nuts" -> return "frutos secos"
            "gluten" -> return "gluten"
            "egg" -> return "huevos"
            "mustard" -> return "mostaza"
            "lupin" -> return "altramuces"
            "celery" -> return "apio"
            "soybeans" -> return "soja"
            "molluscs" -> return "moluscos"
            "crustaceans" -> return "crustáceos"
            "fish" -> return "pescado"
            "so2" -> return "SO₂ y sulfitos"
            "peanuts" -> return "cacahuetes"
            "milk" -> return "leche"
            "sesame" -> return "sésamo"
            else -> ""
        }
    }

    private fun String.toAllergenTag(): String {
        when (this) {
            "nuts" -> return "en:nuts"
            "gluten" -> return "en:gluten"
            "egg" -> return "en:eggs"
            "mustard" -> return "en:mustard"
            "lupin" -> return "en:lupin"
            "celery" -> return "en:celery"
            "soybeans" -> return "en:soybeans"
            "molluscs" -> return "en:molluscs"
            "crustaceans" -> return "en:crustaceans"
            "fish" -> return "en:fish"
            "so2" -> return "en:sulphur-dioxide-and-sulphites"
            "peanuts" -> return "en:peanuts"
            "milk" -> return "en:milk"
            "sesame" -> return "en:sesame-seeds"
            else -> return ""
        }
    }

}