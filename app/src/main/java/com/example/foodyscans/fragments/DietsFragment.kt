package com.example.foodyscans.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.foodyscans.PrincipalFormActivity
import com.example.foodyscans.databinding.FragmentDietsBinding
import com.example.foodyscans.models.FormViewModel


class DietsFragment : Fragment() {

    private lateinit var binding: FragmentDietsBinding
    private val formViewModel: FormViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDietsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (formViewModel.diet) {
            "no" -> binding.no.isChecked = true
            "Vegano" -> binding.vegano.isChecked = true
            "Vegetariano" -> binding.vegetariano.isChecked = true
        }

        val nutrimentsMapping = mapOf(
            "fats" to Triple(binding.lowFats, binding.moderateFats, binding.highFats),
            "saturated-fats" to Triple(binding.lowSfats, binding.moderateSfats, binding.highSfats),
            "sugars" to Triple(binding.lowSugars, binding.moderateSugars, binding.highSugars),
            "salt" to Triple(binding.lowSalt, binding.moderateSalt, binding.highSalt)
        )

        for ((key, buttons) in nutrimentsMapping) {
            when (formViewModel.nutriments[key]) {
                "low" -> buttons.first.isChecked = true
                "moderate" -> buttons.second.isChecked = true
                "high" -> buttons.third.isChecked = true
            }
        }

        for (i in formViewModel.otherDiets) {
            when (i) {
                "aceite de palma" -> binding.palmOil.isChecked = true
                "carne de ternera" -> binding.beef.isChecked = true
                "carne de cerdo" -> binding.pork.isChecked = true
            }
        }

        binding.radioDietsGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.vegano.id -> formViewModel.diet = "Vegano"
                binding.vegetariano.id -> formViewModel.diet = "Vegetariano"
                binding.no.id -> formViewModel.diet = "no"
            }
        }

        binding.radioFatsGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.lowFats.id -> formViewModel.nutriments["fats"] = "low"
                binding.moderateFats.id -> formViewModel.nutriments["fats"] = "moderate"
                binding.highFats.id -> formViewModel.nutriments["fats"] = "high"
            }
        }

        binding.radioSaturatedFatsGroup.setOnCheckedChangeListener{ _, checkedId ->
            when (checkedId) {
                binding.lowSfats.id -> formViewModel.nutriments["saturated-fats"] = "low"
                binding.moderateSfats.id -> formViewModel.nutriments["saturated-fats"] = "moderate"
                binding.highSfats.id -> formViewModel.nutriments["saturated-fats"] = "high"
            }
        }

        binding.radioSugarsGroup.setOnCheckedChangeListener{ _, checkedId ->
            when (checkedId) {
                binding.lowSugars.id -> formViewModel.nutriments["sugars"] = "low"
                binding.moderateSugars.id -> formViewModel.nutriments["sugars"] = "moderate"
                binding.highSugars.id -> formViewModel.nutriments["sugars"] = "high"
            }
        }

        binding.radioSaltGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.lowSalt.id -> formViewModel.nutriments["salt"] = "low"
                binding.moderateSalt.id -> formViewModel.nutriments["salt"] = "moderate"
                binding.highSalt.id -> formViewModel.nutriments["salt"] = "high"
            }
        }

        binding.forwardButton.setOnClickListener {
            formViewModel.otherDiets = mutableListOf()
            if (binding.palmOil.isChecked && "aceite de palma" !in formViewModel.otherDiets) formViewModel.otherDiets.add("aceite de palma")
            if (binding.beef.isChecked && "carne de ternera" !in formViewModel.otherDiets) formViewModel.otherDiets.add("carne de ternera")
            if (binding.pork.isChecked && "carne de cerdo" !in formViewModel.otherDiets) formViewModel.otherDiets.add("carne de cerdo")
            (activity as PrincipalFormActivity).setFragment(4)
        }

        binding.backButton.setOnClickListener {
            formViewModel.otherDiets = mutableListOf()
            if (binding.palmOil.isChecked && "aceite de palma" !in formViewModel.otherDiets) formViewModel.otherDiets.add("aceite de palma")
            if (binding.beef.isChecked && "carne de ternera" !in formViewModel.otherDiets) formViewModel.otherDiets.add("carne de ternera")
            if (binding.pork.isChecked && "carne de cerdo" !in formViewModel.otherDiets) formViewModel.otherDiets.add("carne de cerdo")
            (activity as PrincipalFormActivity).setFragment(2)
        }

    }

}