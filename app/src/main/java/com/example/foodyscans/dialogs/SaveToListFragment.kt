package com.example.foodyscans.dialogs

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodyscans.adapters.SaveToListAdapter
import com.example.foodyscans.databinding.FragmentSaveToListBinding
import com.example.foodyscans.models.Ingredient
import com.example.foodyscans.models.Product
import com.example.foodyscans.models.ProductList
import com.example.foodyscans.models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class SaveToListFragment(private val product: Product) : DialogFragment(), SaveToListAdapter.OnItemCheckedListener {

    private lateinit var binding: FragmentSaveToListBinding
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var mListAdapter: SaveToListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    private val productLists = mutableListOf<ProductList>()
    private val initialCheckedItems = mutableListOf<ProductList>()
    private val checkedItems = mutableListOf<ProductList>()

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout((resources.displayMetrics.widthPixels*0.9).toInt(), LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaveToListBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            productLists.clear()
            productLists.addAll(user.lists.sortedBy { it.name.lowercase() }.filter { it.name != "Favoritos" })
            initialCheckedItems.addAll(user.lists.sortedBy { it.name.lowercase() }.filter { it -> it.products.any { it.id == product.id } })
            checkedItems.addAll(initialCheckedItems)
            setRecycler()
        })

        binding.newItem.setOnClickListener {
            val dialogFragment = CreateSaveListFragment()
            dialogFragment.show(parentFragmentManager, "new")
        }
    }

    private fun setRecycler() {

        mListAdapter = SaveToListAdapter(productLists, checkedItems)
        mListAdapter.setListener(this)
        mLayoutManager = LinearLayoutManager(requireContext())

        binding.listsRecycler.apply {
            layoutManager = mLayoutManager
            adapter = mListAdapter
        }
    }

    override fun onItemChecked(item: ProductList) {
        checkedItems.add(item)
    }

    override fun onItemUnchecked(item: ProductList) {
        checkedItems.remove(item)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        userViewModel.saveToList(initialCheckedItems, checkedItems, product) { success ->
            if (!success) {
                Toast.makeText(requireContext(), "Error al guardar el producto", Toast.LENGTH_SHORT).show()
            }
        }
    }

}