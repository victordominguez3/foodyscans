package com.example.foodyscans.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.foodyscans.PrincipalFormActivity
import com.example.foodyscans.R
import com.example.foodyscans.databinding.FragmentScoresBinding
import com.example.foodyscans.dialogs.EditProfileFragment
import com.example.foodyscans.dialogs.PopUpInfoFragment
import com.example.foodyscans.models.FormViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class ScoresFragment : Fragment() {

    private lateinit var binding: FragmentScoresBinding
    private val formViewModel: FormViewModel by activityViewModels()

    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    private var selectedNutriScore = ""
    private var selectedNovaScore = ""
    private var selectedEcoScore = ""

    private var nutriScoreButtons = listOf<CardView>()
    private var novaScoreButtons = listOf<CardView>()
    private var ecoScoreButtons = listOf<CardView>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (formViewModel.isFirstTimeEditing) setFormViewModel()

        setData()

        configureButtons()

        binding.forwardButton.isEnabled = false

        allSelected()

        binding.forwardButton.setOnClickListener {
            formViewModel.nutriscore = selectedNutriScore
            formViewModel.novascore = selectedNovaScore
            formViewModel.ecoscore = selectedEcoScore
            (activity as PrincipalFormActivity).setFragment(2)
        }

        binding.nutriscoreInfo.setOnClickListener {
            val dialogFragment = PopUpInfoFragment("Nutri-Score", ContextCompat.getString(requireContext(), R.string.nutriscore_info))
            dialogFragment.show(parentFragmentManager, "")
        }

        binding.novaInfo.setOnClickListener {
            val dialogFragment = PopUpInfoFragment("Grupo Nova", ContextCompat.getString(requireContext(), R.string.nova_info))
            dialogFragment.show(parentFragmentManager, "")
        }

        binding.ecoscoreInfo.setOnClickListener {
            val dialogFragment = PopUpInfoFragment("Eco-Score", ContextCompat.getString(requireContext(), R.string.ecoscore_info))
            dialogFragment.show(parentFragmentManager, "")
        }

    }

    private fun configureButtons() {

        nutriScoreButtons = listOf(
            binding.nutriscoreAButton,
            binding.nutriscoreBButton,
            binding.nutriscoreCButton,
            binding.nutriscoreDButton,
            binding.nutriscoreEButton
        )
        novaScoreButtons = listOf(
            binding.nova1Button,
            binding.nova2Button,
            binding.nova3Button,
            binding.nova4Button
        )
        ecoScoreButtons = listOf(
            binding.ecoscoreAButton,
            binding.ecoscoreBButton,
            binding.ecoscoreCButton,
            binding.ecoscoreDButton,
            binding.ecoscoreEButton
        )

        binding.nutriscoreAButton.setOnClickListener {
            resetButtons(nutriScoreButtons)
            binding.nutriscoreAButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedNutriScore = "a"
            allSelected()
        }
        binding.nutriscoreBButton.setOnClickListener {
            resetButtons(nutriScoreButtons)
            binding.nutriscoreBButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedNutriScore = "b"
            allSelected()
        }
        binding.nutriscoreCButton.setOnClickListener {
            resetButtons(nutriScoreButtons)
            binding.nutriscoreCButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedNutriScore = "c"
            allSelected()
        }
        binding.nutriscoreDButton.setOnClickListener {
            resetButtons(nutriScoreButtons)
            binding.nutriscoreDButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedNutriScore = "d"
            allSelected()
        }
        binding.nutriscoreEButton.setOnClickListener {
            resetButtons(nutriScoreButtons)
            binding.nutriscoreEButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedNutriScore = "e"
            allSelected()
        }

        binding.nova1Button.setOnClickListener {
            resetButtons(novaScoreButtons)
            binding.nova1Button.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedNovaScore = "1"
            allSelected()
        }
        binding.nova2Button.setOnClickListener {
            resetButtons(novaScoreButtons)
            binding.nova2Button.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedNovaScore = "2"
            allSelected()
        }
        binding.nova3Button.setOnClickListener {
            resetButtons(novaScoreButtons)
            binding.nova3Button.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedNovaScore = "3"
            allSelected()
        }
        binding.nova4Button.setOnClickListener {
            resetButtons(novaScoreButtons)
            binding.nova4Button.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedNovaScore = "4"
            allSelected()
        }

        binding.ecoscoreAButton.setOnClickListener {
            resetButtons(ecoScoreButtons)
            binding.ecoscoreAButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedEcoScore = "a"
            allSelected()
        }
        binding.ecoscoreBButton.setOnClickListener {
            resetButtons(ecoScoreButtons)
            binding.ecoscoreBButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedEcoScore = "b"
            allSelected()
        }
        binding.ecoscoreCButton.setOnClickListener {
            resetButtons(ecoScoreButtons)
            binding.ecoscoreCButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedEcoScore = "c"
            allSelected()
        }
        binding.ecoscoreDButton.setOnClickListener {
            resetButtons(ecoScoreButtons)
            binding.ecoscoreDButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedEcoScore = "d"
            allSelected()
        }
        binding.ecoscoreEButton.setOnClickListener {
            resetButtons(ecoScoreButtons)
            binding.ecoscoreEButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
            selectedEcoScore = "e"
            allSelected()
        }
    }

    private fun resetButtons(buttons: List<CardView>) {
        buttons.forEach { button ->
            button.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_1))
        }
    }

    private fun allSelected() {
        if (selectedNutriScore.isNotEmpty() && selectedNovaScore.isNotEmpty() && selectedEcoScore.isNotEmpty()) {
            binding.forwardButton.alpha = 1F
            binding.forwardButton.isEnabled = true
        }
    }

    private fun setFormViewModel() {
        if (auth.currentUser != null) {
            db.collection("users").get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        if (document.id == auth.currentUser!!.uid) {
                            if (document.getBoolean("initialForm") == true) {
                                val allergens = document.get("allergens")
                                val diet = document.getString("diet")
                                val otherDiets = document.get("otherDiets")
                                val nutriments = document.get("nutriments")
                                val nutriscore = document.getString("nutriscore")
                                val novascore = document.getString("novascore")
                                val ecoscore = document.getString("ecoscore")
                                if (allergens != null) formViewModel.allergens = allergens as MutableList<String>
                                if (diet != null) formViewModel.diet = diet
                                if (otherDiets != null) formViewModel.otherDiets = otherDiets as MutableList<String>
                                if (nutriments != null) formViewModel.nutriments = nutriments as MutableMap<String, String>
                                if (nutriscore != null) formViewModel.nutriscore = nutriscore
                                if (novascore != null) formViewModel.novascore = novascore
                                if (ecoscore != null) formViewModel.ecoscore = ecoscore
                                formViewModel.initialForm = false
                                formViewModel.isFirstTimeEditing = false
                                setData()
                                allSelected()
                            }
                        }
                    }
                }
        }
    }

    private fun setData() {
        if (formViewModel.nutriscore.isNotEmpty() && formViewModel.novascore.isNotEmpty() && formViewModel.ecoscore.isNotEmpty()) {
            when (formViewModel.nutriscore) {
                "a" -> {
                    binding.nutriscoreAButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedNutriScore = "a"
                }
                "b" -> {
                    binding.nutriscoreBButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedNutriScore = "b"
                }
                "c" -> {
                    binding.nutriscoreCButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedNutriScore = "c"
                }
                "d" -> {
                    binding.nutriscoreDButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedNutriScore = "d"
                }
                "e" -> {
                    binding.nutriscoreEButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedNutriScore = "e"
                }
            }
            when (formViewModel.novascore) {
                "1" -> {
                    binding.nova1Button.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedNovaScore = "1"
                }
                "2" -> {
                    binding.nova2Button.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedNovaScore = "2"
                }
                "3" -> {
                    binding.nova3Button.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedNovaScore = "3"
                }
                "4" -> {
                    binding.nova4Button.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedNovaScore = "4"
                }
            }
            when (formViewModel.ecoscore) {
                "a" -> {
                    binding.ecoscoreAButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedEcoScore = "a"
                }
                "b" -> {
                    binding.ecoscoreBButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedEcoScore = "b"
                }
                "c" -> {
                    binding.ecoscoreCButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedEcoScore = "c"
                }
                "d" -> {
                    binding.ecoscoreDButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedEcoScore = "d"
                }
                "e" -> {
                    binding.ecoscoreEButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_green_5))
                    selectedEcoScore = "e"
                }
            }
        }
    }

}