package com.example.inventory

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.inventory.data.AllProducts
import com.example.inventory.databinding.FragmentAddAllProductsBinding
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
/*private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"*/

/**
 * A simple [Fragment] subclass.
 * Use the [AddAllProductsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddAllProductsFragment : Fragment() {

    private val viewModel: AllProductsViewModel by activityViewModels {
        AllProductsViewModelFactory(
            (activity?.application as InventoryApplication).database
                .allProductsDao()
        )
    }
    private val navigationArgs: ItemDetailFragmentArgs by navArgs()
    private val args: AddAllProductsFragmentArgs by navArgs()

    private lateinit var product: AllProducts
    lateinit var barcode: AllProducts

    // Binding object instance corresponding to the fragment_add_item.xml layout
    // This property is non-null between the onCreateView() and onDestroyView() lifecycle callbacks,
    // when the view hierarchy is attached to the fragment
    private var _binding: FragmentAddAllProductsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAllProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.itemName.text.toString(),
            binding.itemBarcode.text.toString(),
            binding.itemPrice.text.toString(),
            binding.itemCount.text.toString(),
        )
    }

    /**
     * Binds views with the passed in [item] information.
     */
    private fun bind(product: AllProducts) {
        val price = "%.2f".format(product.productPrice)
        binding.apply {
            itemName.setText(product.productName, TextView.BufferType.SPANNABLE)
            itemBarcode.setText(product.productBarcode, TextView.BufferType.SPANNABLE)
            itemPrice.setText(price, TextView.BufferType.SPANNABLE)
            itemCount.setText(product.productQuantityInStock.toString(), TextView.BufferType.SPANNABLE)
            saveAction.setOnClickListener { updateItem() }
        }
    }

    /**
     * Inserts the new Item into database and navigates up to list fragment.
     */
    private fun addNewItem() {
        if (isEntryValid()) {
            viewModel.addNewItem(
                binding.itemName.text.toString(),
                binding.itemBarcode.text.toString(),
                binding.itemPrice.text.toString(),
                binding.itemCount.text.toString(),
            )
            val action = AddAllProductsFragmentDirections.actionAddAllProductsFragmentToItemListFragment()
            findNavController().navigate(action)
        }
    }

    /**
     * Updates an existing Item in the database and navigates up to list fragment.
     */
    private fun updateItem() {
        if (isEntryValid()) {
            viewModel.updateAllProducts(
                this.navigationArgs.itemId,
                this.binding.itemName.text.toString(),
                this.binding.itemBarcode.text.toString(),
                this.binding.itemPrice.text.toString(),
                this.binding.itemCount.text.toString()
            )
            val action = AddAllProductsFragmentDirections.actionAddAllProductsFragmentToItemListFragment()
            findNavController().navigate(action)
        }
    }

    private fun barcodeExists(){
        val barcodeValue = binding.itemBarcode.text.toString()
        if (isEntryValid())
        viewModel.retrieveMatchingBarcode(barcodeValue)
            .observe(this.viewLifecycleOwner) { barcodeTest ->
                try {
                    barcode = barcodeTest
                    Toast.makeText(activity, "Már létezik termék az adott vonalkóddal!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    addNewItem()
                }

            }
    }

    /**
     * Called when the view is created.
     * The itemId Navigation argument determines the edit item  or add new item.
     * If the itemId is positive, this method retrieves the information from the database and
     * allows the user to update it.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.itemId
        val barcode = args.productBarcode
        binding.itemBarcode.setText(barcode)
        if (id > 0) {
            viewModel.retrieveItem(id).observe(this.viewLifecycleOwner) { selectedProduct ->
                product = selectedProduct
                bind(product)
            }
        } else {
            binding.saveAction.setOnClickListener {
                barcodeExists()
            }
        }
    }

    /**
     * Called before fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // Hide keyboard.
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }
}