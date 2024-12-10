package com.example.foodyscans.dialogs

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodyscans.R
import com.example.foodyscans.adapters.ProductAdapter
import com.example.foodyscans.adapters.ProductListener
import com.example.foodyscans.databinding.FragmentSaveListBinding
import com.example.foodyscans.models.Product
import com.example.foodyscans.models.ProductList
import com.example.foodyscans.models.User
import com.example.foodyscans.models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class SaveListFragment(private val productList: ProductList) : DialogFragment(), ProductListener {

    private lateinit var binding: FragmentSaveListBinding
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var mListAdapter: ProductAdapter
    private lateinit var mLayoutManager: GridLayoutManager

    private var user = User()

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
        binding = FragmentSaveListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            this.user = user
            setRecycler()
        })

        binding.title.text = productList.name

    }

    private fun setRecycler() {

        mListAdapter = ProductAdapter(user.lists.find { it.name == productList.name }?.products ?: mutableListOf(), this, user)
        mLayoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)

        binding.productsRecycler.apply {
            layoutManager = mLayoutManager
            adapter = mListAdapter
        }
    }

    override fun onClick(product: Product) {
        val dialogFragment = ProductFragment(product)
        dialogFragment.show(parentFragmentManager, "")
    }

    override fun onLongClick(product: Product) {

        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Alerta").setMessage("¿Quitar ${product.product_name} de esta lista?")

        builder.setPositiveButton("Sí") { _, _ ->
            userViewModel.removeProductFromList(product, productList) { success ->
                if (!success) {
                    Toast.makeText(requireContext(), "Error al quitar el producto de la lista", Toast.LENGTH_SHORT).show()
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