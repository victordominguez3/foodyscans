package com.example.foodyscans.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.foodyscans.MainActivity
import com.example.foodyscans.PrincipalFormActivity
import com.example.foodyscans.R
import com.example.foodyscans.databinding.FragmentProfileBinding
import com.example.foodyscans.dialogs.EditProfileFragment
import com.example.foodyscans.dialogs.LogOutFragment
import com.example.foodyscans.dialogs.SettingsFragment
import com.example.foodyscans.models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.File

class ProfileFragment : Fragment(), LogOutFragment.OnLogOutListener, EditProfileFragment.OnEditProfileListener {

    private lateinit var binding: FragmentProfileBinding
    private val userViewModel: UserViewModel by activityViewModels()

    private val auth = Firebase.auth

    private lateinit var activityLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            binding.name.text = user.name
            binding.mail.text = user.mail
            when (user.nutriscore) {
                "a" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_a)
                "b" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_b)
                "c" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_c)
                "d" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_d)
                "e" -> binding.nutriscoreImage.setImageResource(R.drawable.nutri_score_e)
            }
            when (user.novascore) {
                "1" -> binding.novaImage.setImageResource(R.drawable.nova_group_1)
                "2" -> binding.novaImage.setImageResource(R.drawable.nova_group_2)
                "3" -> binding.novaImage.setImageResource(R.drawable.nova_group_3)
                "4" -> binding.novaImage.setImageResource(R.drawable.nova_group_4)
            }
            when (user.ecoscore) {
                "a" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_a)
                "b" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_b)
                "c" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_c)
                "d" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_d)
                "e" -> binding.ecoscoreImage.setImageResource(R.drawable.eco_score_e)
            }
            if (user.allergens.isNotEmpty()) {

                val text = if (user.allergens.size > 1) {
                    val first = user.allergens.dropLast(1).joinToString(", ") { getName(it) }
                    val last = getName(user.allergens.last().toString())
                    "${first.replaceFirstChar { it.uppercase() }} y $last"
                } else {
                    user.allergens.joinToString { getName(it) }.replaceFirstChar { it.uppercase() }
                }
                binding.allergensText.text = text
            } else {
                binding.allergensText.text = "No"
            }
            val diet = user.diet
            if ((diet.isNotEmpty() && diet != "no") || user.otherDiets.isNotEmpty()) {
                var text = ""
                if (diet.isNotEmpty() && diet != "no") text = diet
                if (user.otherDiets.isNotEmpty()) {
                    val otherDiets = user.otherDiets
                    text += if (diet.isNotEmpty() && diet != "no") "\nDieta sin " else "Dieta sin "
                    when (otherDiets.count()) {
                        1 -> text += otherDiets[0]
                        2 -> text += otherDiets[0] + " ni " + otherDiets[1]
                        3 -> text += otherDiets[0] + ", " + otherDiets[1] + " ni " + otherDiets[2]
                    }
                }
                binding.dietsText.text = text
            } else {
                binding.dietsText.text = "No"
            }

            binding.fatsText.text = mapOf("low" to "Bajo", "moderate" to "Moderado", "high" to "Alto").getOrDefault(user.nutriments["fats"], "Desconocido")
            binding.saturatedFatsText.text = mapOf("low" to "Bajo", "moderate" to "Moderado", "high" to "Alto").getOrDefault(user.nutriments["saturated-fats"], "Desconocido")
            binding.sugarsText.text = mapOf("low" to "Bajo", "moderate" to "Moderado", "high" to "Alto").getOrDefault(user.nutriments["sugars"], "Desconocido")
            binding.saltText.text = mapOf("low" to "Bajo", "moderate" to "Moderado", "high" to "Alto").getOrDefault(user.nutriments["salt"], "Desconocido")

        })

        setImage()

        activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val user = userViewModel.userData.value?.copy(
                        nutriscore = data.getStringExtra("nutriscore")!!,
                        novascore = data.getStringExtra("novascore")!!,
                        ecoscore = data.getStringExtra("ecoscore")!!,
                        diet = data.getStringExtra("diet")!!,
                        allergens = data.getStringArrayListExtra("allergens") ?: ArrayList(),
                        otherDiets = data.getStringArrayListExtra("otherDiets") ?: ArrayList(),
                        nutriments = data.getSerializableExtra("nutriments") as? MutableMap<String, String> ?: mutableMapOf()
                    )
                    if (user != null) {
                        userViewModel.updateUserData(user)
                    }
                }
            }
        }

        binding.opciones.setOnClickListener {
            showMenu(it)
        }

    }

    private fun setImage() {

        val fileName = "profile_${auth.currentUser!!.uid}.jpg"
        val file = File(context?.filesDir, fileName)

        if (file.exists()) {
            Glide.with(binding.image)
                .load(file)
                .transform(CenterCrop(), RoundedCorners(20))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.image)
        } else {
            Log.e("CargarImagen", "El archivo no existe en la ruta: ${file.absolutePath}")
        }
    }

    private fun getName(idName: String): String {
        return when (idName) {
            "nuts" -> return "frutos secos"
            "gluten" -> return "gluten"
            "egg" -> return "huevos"
            "mustard" -> return "mostaza"
            "lupin" -> return "altramuces"
            "celery" -> return "apio"
            "soybeans" -> return "soja"
            "molluscs" -> return "moluscos"
            "crustaceans" -> return "crustáceos"
            "fish" -> return "pescado"
            "so2" -> return "SO₂ y sulfitos"
            "peanuts" -> return "cacahuetes"
            "milk" -> return "leche"
            "sesame" -> return "sésamo"
            else -> ""
        }
    }

    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.popup_settings, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_profile -> {

                    val dialogFragment = EditProfileFragment(this)
                    dialogFragment.show(parentFragmentManager, "")

                    true
                }
                R.id.edit_profile_card -> {

                    val intent = Intent(requireContext(), PrincipalFormActivity::class.java)
                    activityLauncher.launch(intent)

                    true
                }
                R.id.settings -> {

                    val dialogFragment = SettingsFragment()
                    dialogFragment.show(parentFragmentManager, "")

                    true
                }
                R.id.log_out -> {

                    val dialogFragment = LogOutFragment(this)
                    dialogFragment.show(parentFragmentManager, "")

                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    override fun onLogOutClick() {
        auth.signOut()
        activity?.finish()
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onConfirmClick() {
        setImage()
    }

    fun scrollToTop() {
        binding.scroll.smoothScrollTo(0, 0)
    }

}