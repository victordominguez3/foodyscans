package com.example.foodyscans.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import com.example.foodyscans.PrincipalFormActivity
import com.example.foodyscans.R
import com.example.foodyscans.databinding.FragmentAllergensBinding
import com.example.foodyscans.databinding.FragmentScoresBinding
import com.example.foodyscans.models.FormViewModel

class AllergensFragment : Fragment() {

    private lateinit var binding: FragmentAllergensBinding
    private val formViewModel: FormViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllergensBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (formViewModel.allergens.isNotEmpty()) {
            checkBoxes(formViewModel.allergens)
        }

        binding.forwardButton.setOnClickListener {
            formViewModel.allergens = saveChekedBoxes()
            (activity as PrincipalFormActivity).setFragment(3)
        }

        binding.backButton.setOnClickListener {
            formViewModel.allergens = saveChekedBoxes()
            (activity as PrincipalFormActivity).setFragment(1)
        }
    }

    private fun saveChekedBoxes(): MutableList<String> {
        val checkBoxs = mutableListOf<String>()
        val grid = binding.grid
        for (i in 0 until grid.childCount) {
            val child = grid.getChildAt(i)
            if (child is LinearLayout) {
                val checkBox = child.getChildAt(1)
                if (checkBox is CheckBox && checkBox.isChecked) checkBoxs.add(resources.getResourceEntryName(checkBox.id))
            }
        }
        return checkBoxs
    }

    private fun checkBoxes(allergens: MutableList<String>) {
        val grid = binding.grid
        for (i in 0 until grid.childCount) {
            val child = grid.getChildAt(i)
            if (child is LinearLayout) {
                val checkBox = child.getChildAt(1)
                val checkBoxId = resources.getResourceEntryName(checkBox.id)
                if (checkBox is CheckBox) checkBox.isChecked = checkBoxId in allergens
            }
        }
    }

}