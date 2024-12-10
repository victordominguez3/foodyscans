package com.example.foodyscans.dialogs

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.foodyscans.MainActivity
import com.example.foodyscans.databinding.FragmentSettingsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class SettingsFragment : DialogFragment(), DeleteAccountFragment.OnDeleteAccountListener {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

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
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("config", MODE_PRIVATE)

        binding.soundSwitch.isChecked = sharedPreferences.getBoolean("sound_switch_${auth.currentUser!!.uid}", false)
        binding.flashSwitch.isChecked = sharedPreferences.getBoolean("flash_switch_${auth.currentUser!!.uid}", false)

        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("sound_switch_${auth.currentUser!!.uid}", isChecked).apply()
        }

        binding.flashSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("flash_switch_${auth.currentUser!!.uid}", isChecked).apply()
        }

        binding.changePasswordButton.setOnClickListener {
            val dialogFragment = ChangePasswordFragment()
            dialogFragment.show(parentFragmentManager, "")
        }

        binding.deleteAccountButton.setOnClickListener {
            val dialogFragment = DeleteAccountFragment(this)
            dialogFragment.show(parentFragmentManager, "")
        }

    }

    override fun onDeleteAccountClick() {

        val uid = auth.currentUser!!.uid

        auth.currentUser!!.delete()
        db.collection("users").document(uid).delete()
        db.collection("lists").document(uid).delete()
        db.collection("recipes").document(uid).delete()

        val fileName = "profile_${auth.currentUser!!.uid}.jpg"
        val file = File(context?.filesDir, fileName)
        if (file.exists()) file.delete()

        activity?.finish()
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)

    }

}