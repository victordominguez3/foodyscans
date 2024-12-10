package com.example.foodyscans.fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.foodyscans.R
import com.example.foodyscans.adapters.ProductAdapter
import com.example.foodyscans.adapters.ProductListener
import com.example.foodyscans.api.OpenFoodFactsClient
import com.example.foodyscans.databinding.FragmentExploreBinding
import com.example.foodyscans.dialogs.ProductFragment
import com.example.foodyscans.models.ApiResponse
import com.example.foodyscans.models.Product
import com.example.foodyscans.models.User
import com.example.foodyscans.models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ExploreFragment : Fragment(), ProductListener {

    private lateinit var binding: FragmentExploreBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val userViewModel: UserViewModel by activityViewModels()

    private val auth = Firebase.auth
    private var user = User()

    private lateinit var mListAdapter: ProductAdapter
    private lateinit var mLayoutManager: GridLayoutManager

    private var productCode = ""
    private var scannedProduct: Product? = null

    private var currentPage = 1
    private var productsToShow = mutableListOf<Product>()
    private var moreProdcuts = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            this.user = user
            setRecycler()
        })

        setBarcodeActivity()

        binding.scannedProduct.setOnClickListener {
            if (scannedProduct != null) {
                val dialogFragment = ProductFragment(scannedProduct!!)
                dialogFragment.show(parentFragmentManager, "")
            }
        }

        binding.searchText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (binding.searchText.text.toString().trim().length > 2) {
                    loadProducts(isLoadMore = false)
                }
                true
            } else false
        }

        binding.loadMoreButton.setOnClickListener { loadProducts(isLoadMore = true) }

    }

    private fun setBarcodeActivity() {

        sharedPreferences = requireContext().getSharedPreferences("config", MODE_PRIVATE)

        val barcodeLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                if (sharedPreferences.getBoolean("sound_switch_${auth.currentUser!!.uid}", false)) {
                    val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.death)
                    mediaPlayer.start()
                }
                productCode = result.contents
                getProduct()
            }
        }

        val scanOptions = ScanOptions()
        scanOptions.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES)
        scanOptions.setPrompt("Escanea el código de barras de tu producto")
        scanOptions.setBarcodeImageEnabled(false)
        scanOptions.setBeepEnabled(false)
        scanOptions.setTorchEnabled(sharedPreferences.getBoolean("flash_switch_${auth.currentUser!!.uid}", false))

        binding.searchBar.setEndIconOnClickListener { barcodeLauncher.launch(scanOptions) }
    }

    private fun getProduct() {
        val call = OpenFoodFactsClient.instance.getProduct(productCode)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val product = response.body()
                    if (product?.product != null) {
                        scannedProduct = product.product
                        val dialogFragment = ProductFragment(product.product)
                        dialogFragment.show(parentFragmentManager, "")
                        setScannedProduct(product.product)
                        setRecommendedProducts(product.product, false)
                        binding.titleText.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                println("Error: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setRecycler() {

        mListAdapter = ProductAdapter(productsToShow, this, user)
        mLayoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)

        binding.productsRecycler.apply {
            layoutManager = mLayoutManager
            adapter = mListAdapter
        }
    }

    private fun setScannedProduct(product: Product) {

        try {
            Glide.with(binding.scannedImage)
                .load(product.image_url)
                .transform(CenterCrop(), RoundedCorners(20))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.scannedImage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.scannedName.text = product.product_name
        binding.scannedBrand.text = product.brands
        binding.scannedQuantity.text = product.quantity

        binding.scannedProduct.visibility = View.VISIBLE
    }

    private fun loadProducts(isLoadMore: Boolean) {
        val searchQuery = binding.searchText.text.toString().trim()

        fun fetchPage(page: Int) {
            val call = OpenFoodFactsClient.instance.getSearchProducts(searchQuery, page = page)

            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        val fetchedProducts = apiResponse?.products ?: emptyList()
                        moreProdcuts.addAll(fetchedProducts)

                        if (moreProdcuts.size >= 50 && apiResponse?.page_count == 50) {
                            productsToShow.addAll(moreProdcuts)
                            moreProdcuts.clear()
                            mListAdapter.setProducts(productsToShow.toMutableList())
                            binding.similarProductsText.visibility = View.GONE
                            binding.scannedProduct.visibility = View.GONE
                            binding.titleText.visibility = View.GONE
                            binding.scroll.visibility = View.VISIBLE
                            currentPage++
                            binding.loadMoreButton.visibility = View.VISIBLE
                        } else if (productsToShow.size < 50 && apiResponse?.page_count == 50) {
                            currentPage++
                            fetchPage(currentPage)
                        } else if (apiResponse?.page_count in 1..50) {
                            productsToShow.addAll(moreProdcuts)
                            moreProdcuts.clear()
                            mListAdapter.setProducts(productsToShow.toMutableList())
                            binding.similarProductsText.visibility = View.GONE
                            binding.scannedProduct.visibility = View.GONE
                            binding.titleText.visibility = View.GONE
                            binding.scroll.visibility = View.VISIBLE
                            currentPage++
                            binding.loadMoreButton.visibility = View.GONE
                        } else {
                            binding.loadMoreButton.visibility = View.GONE
                        }
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    println("Error: ${t.message}")
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        if (!isLoadMore) {
            currentPage = 1
            productsToShow.clear()
            moreProdcuts.clear()
            mListAdapter.setProducts(productsToShow)
            binding.loadMoreButton.visibility = View.GONE
        }

        fetchPage(currentPage)
    }

    private fun setRecommendedProducts(product: Product, isLoadMore: Boolean) {
        val searchQuery = product.product_name?.trim() ?: ""

        fun fetchPage(page: Int) {
            val call = OpenFoodFactsClient.instance.getSearchProducts(searchQuery, page = page)

            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        val fetchedProducts = apiResponse?.products ?: emptyList()
                        moreProdcuts.addAll(fetchedProducts)

                        if (moreProdcuts.size >= 50 && apiResponse?.page_count == 50) {
                            productsToShow.addAll(moreProdcuts)
                            moreProdcuts.clear()
                            mListAdapter.setProducts(productsToShow.toMutableList())
                            binding.similarProductsText.visibility = View.GONE
                            binding.titleText.visibility = View.GONE
                            binding.scroll.visibility = View.VISIBLE
                            currentPage++
                            binding.loadMoreButton.visibility = View.VISIBLE
                        } else if (productsToShow.size < 50 && apiResponse?.page_count == 50) {
                            currentPage++
                            fetchPage(currentPage)
                        } else if (apiResponse?.page_count in 1..50) {
                            productsToShow.addAll(moreProdcuts)
                            moreProdcuts.clear()
                            mListAdapter.setProducts(productsToShow.toMutableList())
                            binding.similarProductsText.visibility = View.GONE
                            binding.titleText.visibility = View.GONE
                            binding.scroll.visibility = View.VISIBLE
                            currentPage++
                            binding.loadMoreButton.visibility = View.GONE
                        } else {
                            binding.loadMoreButton.visibility = View.GONE
                        }
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    println("Error: ${t.message}")
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        if (!isLoadMore) {
            currentPage = 1
            productsToShow.clear()
            moreProdcuts.clear()
            mListAdapter.setProducts(productsToShow)
            binding.loadMoreButton.visibility = View.GONE
        }

        fetchPage(currentPage)
    }

    override fun onClick(product: Product) {
        val dialogFragment = ProductFragment(product)
        dialogFragment.show(parentFragmentManager, "")
    }

    override fun onLongClick(product: Product) {

    }

    fun scrollToTop() {
        binding.scroll.smoothScrollTo(0, 0)
    }

}