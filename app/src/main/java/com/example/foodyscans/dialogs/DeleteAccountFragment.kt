package com.example.foodyscans.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.foodyscans.databinding.FragmentDeleteAccountBinding

class DeleteAccountFragment(private val listener: OnDeleteAccountListener): DialogFragment() {

    private lateinit var binding: FragmentDeleteAccountBinding

    interface OnDeleteAccountListener {
        fun onDeleteAccountClick()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout((resources.displayMetrics.widthPixels*0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeleteAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.deleteButton.setOnClickListener {
            listener.onDeleteAccountClick()
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

    }

}