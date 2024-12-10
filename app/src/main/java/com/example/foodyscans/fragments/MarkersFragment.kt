package com.example.foodyscans.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodyscans.R
import com.example.foodyscans.adapters.SaveListAdapter
import com.example.foodyscans.adapters.SaveListListener
import com.example.foodyscans.databinding.FragmentMarkersBinding
import com.example.foodyscans.dialogs.CreateSaveListFragment
import com.example.foodyscans.dialogs.SaveListFragment
import com.example.foodyscans.models.Ingredient
import com.example.foodyscans.models.NutrientLevels
import com.example.foodyscans.models.Product
import com.example.foodyscans.models.ProductList
import com.example.foodyscans.models.User
import com.example.foodyscans.models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MarkersFragment : Fragment(), SaveListListener {

    private lateinit var binding: FragmentMarkersBinding
    private val userViewModel: UserViewModel by activityViewModels()

    private val favList: MutableList<Product> = mutableListOf()

    private lateinit var mListAdapter: SaveListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMarkersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycler()

        userViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            mListAdapter.setSaveLists(user.lists.sortedBy { it.name.lowercase() }.filter { it.name != "Favoritos" }.toMutableList())
            binding.countFavText.text = "${user.lists.find { it.name == "Favoritos" }?.products?.count()}"
            favList.clear()
            favList.addAll(user.lists.find { it.name == "Favoritos" }?.products ?: emptyList())
        })

        binding.newItem.setOnClickListener {
            val dialogFragment = CreateSaveListFragment()
            dialogFragment.show(parentFragmentManager, "new")
        }

        binding.favList.setOnClickListener {
            if (favList.isNotEmpty()) {
                val dialogFragment = SaveListFragment(ProductList("Favoritos", favList))
                dialogFragment.show(parentFragmentManager, "")
            } else Toast.makeText(requireContext(), "No hay productos", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setRecycler() {

        mListAdapter = SaveListAdapter(mutableListOf(), this)
        mLayoutManager = LinearLayoutManager(requireContext())

        binding.listsRecycler.apply {
            layoutManager = mLayoutManager
            adapter = mListAdapter
        }

    }

    override fun onClick(productList: ProductList) {
        if (productList.products.isNotEmpty()) {
            val dialogFragment = SaveListFragment(productList)
            dialogFragment.show(parentFragmentManager, "")
        } else Toast.makeText(requireContext(), "No hay productos", Toast.LENGTH_SHORT).show()
    }

    override fun onLongClick(productList: ProductList) {

        val builder = AlertDialog.Builder(requireContext())
            .setItems(
                arrayOf("Editar", "Eliminar")
            ) { dialog, which ->
                if (which == 0) {

                    val dialogFragment = CreateSaveListFragment()
                    val bundle = Bundle()
                    bundle.putString("name", productList.name)
                    dialogFragment.arguments = bundle
                    dialogFragment.show(parentFragmentManager, "edit")

                } else {

                    if (productList.products.isEmpty()) {

                        val builder = AlertDialog.Builder(requireContext())

                        builder.setTitle("Alerta").setMessage("¿Está seguro de que desea borrar la lista ${productList.name}?")

                        builder.setPositiveButton("Sí") { _, _ ->
                            userViewModel.deleteSaveList(productList) { success ->
                                if (!success) {
                                    Toast.makeText(requireContext(), "Ocurrió un error al eliminar la lista", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        builder.setNegativeButton("No") { _, _ ->

                        }

                        val dialog = builder.create()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_shape)
                        dialog.show()

                    } else {

                        val builder = AlertDialog.Builder(requireContext())

                        builder.setTitle("Alerta")
                            .setMessage("La lista contiene productos. ¿Está seguro de que desea borrar la lista?")

                        builder.setPositiveButton("Sí") { _, _ ->
                            userViewModel.deleteSaveList(productList) { success ->
                                if (!success) {
                                    Toast.makeText(requireContext(), "Ocurrió un error al eliminar la lista", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        builder.setNegativeButton("No") { _, _ ->

                        }

                        val dialog = builder.create()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_shape)
                        dialog.show()
                    }
                }
            }
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_shape)
        dialog.show()
    }

    fun scrollToTop() {
        binding.scroll.smoothScrollTo(0, 0)
    }
}