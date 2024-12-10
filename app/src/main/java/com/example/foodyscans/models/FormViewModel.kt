package com.example.foodyscans.models

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class FormViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    var nutriscore: String = ""
    var novascore: String = ""
    var ecoscore: String = ""
    var allergens: MutableList<String> = mutableListOf()
    var diet: String = "no"
    var otherDiets: MutableList<String> = mutableListOf()
    var nutriments: MutableMap<String, String> = mutableMapOf(
        "fats" to "high",
        "saturated-fats" to "high",
        "sugars" to "high",
        "salt" to "high"
    )
    var initialForm: Boolean = true
    var isFirstTimeEditing: Boolean = true

    fun saveData(newData: Map<String, Any?>, callback: (Boolean) -> Unit) {
        if (auth.currentUser != null) {
            db.collection("users").document(auth.currentUser!!.uid).update(newData).addOnSuccessListener {
                callback(true)
            }.addOnFailureListener { callback(false) }
        } else callback(false)
    }
}