package com.example.foodyscans.dialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.foodyscans.R
import com.example.foodyscans.databinding.FragmentChangePasswordBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth

class ChangePasswordFragment: DialogFragment() {

    private lateinit var binding: FragmentChangePasswordBinding

    private val auth = Firebase.auth

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout((resources.displayMetrics.widthPixels*0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.confirmButton.setOnClickListener {

            if (auth.currentUser != null) {
                val mail = auth.currentUser!!.email
                if (mail != null) {
                    if (binding.actualPassword.text.toString().isNotBlank()) {

                        val credentials = EmailAuthProvider.getCredential(mail, binding.actualPassword.text.toString())

                        auth.currentUser!!.reauthenticate(credentials)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    if (binding.newPassword.text.toString().isNotBlank() && binding.repeatPassword.text.toString().isNotBlank() &&
                                        binding.newPassword.text.toString() == binding.repeatPassword.text.toString()) {

                                        auth.currentUser!!.updatePassword(binding.newPassword.text.toString())
                                            .addOnSuccessListener {
                                                Toast.makeText(requireContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                                                dismiss()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                                            }

                                    } else {
                                        Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "La contraseña actual no es correcta", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else Toast.makeText(requireContext(), "Debes introducir la contraseña actual", Toast.LENGTH_SHORT).show()
                }
            }

        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

    }

}