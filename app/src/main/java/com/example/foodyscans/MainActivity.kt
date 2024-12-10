package com.example.foodyscans

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.foodyscans.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_green_1)

        if (auth.currentUser != null) {
            if (sharedPreferences.getBoolean("${auth.currentUser!!.uid}_initialForm", false)) {
                intent = Intent(this, AppActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                intent = Intent(this, PrincipalFormActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.loginButton.setOnClickListener {

            if (binding.mail.text.toString().isBlank() || binding.password.text.toString().isBlank()) {
                binding.errorMessage.visibility = View.VISIBLE
            } else {

                auth.signInWithEmailAndPassword(
                    binding.mail.text.toString(),
                    binding.password.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            binding.errorMessage.visibility = View.INVISIBLE

                            if (sharedPreferences.getBoolean("${auth.currentUser!!.uid}_initialForm", false)) {
                                intent = Intent(this, AppActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                intent = Intent(this, PrincipalFormActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                        } else {
                            binding.errorMessage.visibility = View.VISIBLE
                        }
                    }
                    .addOnFailureListener(this) {
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.registerText.setOnClickListener {
            intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}