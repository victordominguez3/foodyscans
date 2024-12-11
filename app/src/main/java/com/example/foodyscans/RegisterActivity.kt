package com.example.foodyscans

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.foodyscans.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_green_1)

        binding.registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {

        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE)

        val mailRegex =
            Regex("^[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\$")

        if (binding.username.text.toString().isBlank() ||
            !binding.mail.text.toString().matches(mailRegex) ||
            binding.password.text.toString().isBlank() ||
            binding.repeatPassword.text.toString().isBlank() ||
            binding.password.text.toString() != binding.repeatPassword.text.toString()
        ) {

            if (binding.password.text.toString().isBlank() ||
                binding.repeatPassword.text.toString().isBlank() ||
                binding.password.text.toString() != binding.repeatPassword.text.toString()
            ) {

                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            }

        } else {

            val user = hashMapOf(
                "name" to binding.username.text.toString(),
                "mail" to binding.mail.text.toString(),
                "nutriscore" to "",
                "novascore" to "",
                "ecoscore" to "",
                "allergens" to mutableListOf<String>(),
                "diet" to mutableListOf<String>(),
                "otherDiets" to mutableListOf<String>(),
                "initialForm" to false
            )

            auth.createUserWithEmailAndPassword(
                binding.mail.text.toString(),
                binding.password.text.toString()
            )
                .addOnCompleteListener(this) { task ->
                    Log.e("sf", "pasa por complete")
                    if (task.isSuccessful) {

                        if (auth.currentUser != null) {

                            val uid = auth.currentUser!!.uid

                            val doc = db.collection("users").document(uid)

                            val listsDocumentRef = db.collection("lists").document(auth.currentUser!!.uid)
                            val recipesDocumentRef = db.collection("recipes").document(auth.currentUser!!.uid)

                            listsDocumentRef.set(mapOf("Favoritos" to FieldValue.arrayUnion()))
                            recipesDocumentRef.set(mapOf("Generadas por ChatGPT" to FieldValue.arrayUnion(), "Mis recetas" to FieldValue.arrayUnion()))

                            doc.set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Usuario creado con éxito",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    sharedPreferences.edit().putBoolean("flash_switch_${auth.currentUser!!.uid}", false).apply()
                                    sharedPreferences.edit().putBoolean("sound_switch_${auth.currentUser!!.uid}", false).apply()
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "DB failed",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                }

        }
    }
}