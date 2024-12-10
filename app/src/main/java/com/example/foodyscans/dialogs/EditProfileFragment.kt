package com.example.foodyscans.dialogs

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.foodyscans.databinding.FragmentEditProfileBinding
import com.example.foodyscans.models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

class EditProfileFragment(private val listener: OnEditProfileListener) : DialogFragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private val userViewModel: UserViewModel by activityViewModels()

    private val auth = Firebase.auth

    private var imageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri->
        if (uri != null) {
            imageUri = uri
            binding.galeria.setImageURI(imageUri)
        }
    }

    interface OnEditProfileListener {
        fun onConfirmClick()
    }

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
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            binding.name.setText(user.name)
        })

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        setImage()

        binding.galeria.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.confirmButton.setOnClickListener {

            val newData = hashMapOf(
                "name" to binding.name.text.toString()
            )

            userViewModel.editProfile(newData, binding.name.text.toString()) { success ->
                if (success) {

                    if (imageUri != null) {
                        val fileName = "profile_${auth.currentUser!!.uid}.jpg"
                        val file = File(context?.filesDir, fileName)

                        try {
                            val outputStream = FileOutputStream(file)
                            val inputStream = context?.contentResolver?.openInputStream(imageUri!!)
                            inputStream?.copyTo(outputStream)
                            inputStream?.close()
                            outputStream.close()
                            listener.onConfirmClick()
                            dismiss()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    if (imageUri == null) {
                        listener.onConfirmClick()
                        dismiss()
                    }

                } else {
                    Toast.makeText(context, "Error al editar el perfil", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun setImage() {

        val fileName = "profile_${auth.currentUser!!.uid}.jpg"
        val file = File(context?.filesDir, fileName)

        if (file.exists()) {
            Glide.with(binding.galeria)
                .load(file)
                .transform(CenterCrop(), RoundedCorners(20))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.galeria)
        } else {
            Log.e("CargarImagen", "El archivo no existe en la ruta: ${file.absolutePath}")
        }

    }

}