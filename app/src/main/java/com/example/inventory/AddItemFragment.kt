/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.inventory

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.inventory.data.AllProducts
import com.example.inventory.data.Item
import com.example.inventory.databinding.FragmentAddItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * Fragment to add or update an item in the Inventory database.
 */
class AddItemFragment : Fragment() {

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    // to share the ViewModel across fragments.
    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database
                .itemDao()
        )
    }
    private val allProductsViewModel : AllProductsViewModel by activityViewModels{
        AllProductsViewModelFactory(
            (activity?.application as InventoryApplication).database.
        allProductsDao())
    }
    private val navigationArgs: ItemDetailFragmentArgs by navArgs()

    lateinit var item: Item
    lateinit var barcode: AllProducts
    private var quantity: Int = 0

    // Binding object instance corresponding to the fragment_add_item.xml layout
    // This property is non-null between the onCreateView() and onDestroyView() lifecycle callbacks,
    // when the view hierarchy is attached to the fragment
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
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
    private fun bind(item: Item) {
        //val price = "%.2f".format(item.itemPrice)
        binding.apply {
            itemName.setText(item.itemAruid, TextView.BufferType.SPANNABLE)
            itemBarcode.setText(item.itemVonalkod, TextView.BufferType.SPANNABLE)
            itemPrice.setText(item.id, TextView.BufferType.SPANNABLE)
            itemCount.setText(item.itemKarton.toString(), TextView.BufferType.SPANNABLE)
            saveAction.setOnClickListener { updateItem() }
            /*btnIncreaseQuantity.setOnClickListener { increaseQuantity() }
            btnDecreaseQuantity.setOnClickListener { decreaseQuantity() }*/
            showItemWithBarcodeAction.setOnClickListener{ showItemWithBarcode() }
            itemBarcode.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    showItemWithBarcode()
                }
            }
        }
    }

    private fun bindBarcode(barcode: AllProducts) {
        binding.apply { itemName.setText(barcode.productCikknev, TextView.BufferType.SPANNABLE)
            itemPrice.setText(barcode.productBrfogyar.toString(), TextView.BufferType.SPANNABLE)
        }
    }

    /**
     * Inserts the new Item into database and navigates up to list fragment.
     */
    private fun addNewItem() {
        if (isEntryValid()) {
            val barcodeValue = binding.itemBarcode.text.toString()
            allProductsViewModel.retrieveMatchingBarcode(barcodeValue)
                .observe(this.viewLifecycleOwner) { barcodeTest ->
                    try {
                        barcode = barcodeTest
                        //bindBarcode(barcode)
                        viewModel.addNewItem(
                            binding.itemName.text.toString().toInt(),
                            binding.itemBarcode.text.toString(),
                            binding.itemCount.text.toString().toInt(),
                        )
                        val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
                        findNavController().navigate(action)
                    } catch (e: Exception){
                        showNewItemConfirmationDialog()
                    }
                }

        }
        else{
            Toast.makeText(activity, "Kérem töltse ki az összes mezőt!", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Updates an existing Item in the database and navigates up to list fragment.
     */
    private fun updateItem() {
        if (isEntryValid()) {
            viewModel.updateItem(
                this.navigationArgs.itemId,
                this.binding.itemName.text.toString().toInt(),
                this.binding.itemBarcode.text.toString(),
                this.binding.itemCount.text.toString().toInt()
            )
            val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
            findNavController().navigate(action)
        }
        else{
            Toast.makeText(activity, "Kérem töltse ki az összes mezőt!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun increaseQuantity(){
        quantity += 1
        binding.itemCount.setText(quantity.toString())
    }

    private fun decreaseQuantity(){
        quantity -= 1
        binding.itemCount.setText(quantity.toString())
    }
    private fun showItemWithBarcode() {
        val barcodeValue = binding.itemBarcode.text.toString()
        if (barcodeValue.isNotEmpty())
            allProductsViewModel.retrieveMatchingBarcode(barcodeValue)
                .observe(this.viewLifecycleOwner) { barcodeTest ->
                    try {
                        barcode = barcodeTest
                        bindBarcode(barcode)
                    } catch (e: Exception){
                        showNewItemConfirmationDialog()
                    }

                }
        else{
            Toast.makeText(activity, "Kérem olvassa be a vonalkódot", Toast.LENGTH_SHORT).show()
        }
        /*if (binding.itemBarcode.text.toString() == "a")
        {
            Toast.makeText(activity, "a", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(activity, "nem a", Toast.LENGTH_SHORT).show()
        }*/
    }

    private fun showNewItemConfirmationDialog() {
        val productBarcode = binding.itemBarcode.text.toString()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage("Nem található a vonalkód az adatbázisban. Szeretne hozzáadni egy új terméket?")
            .setCancelable(false)
            .setNegativeButton("Nem") { _, _ -> }
            .setPositiveButton("Igen") { _, _ -> val action =
                AddItemFragmentDirections.actionAddItemFragmentToAddAllProductsFragment(0, productBarcode)
                findNavController().navigate(action)
            }
            .show()
    }

    fun removeData(){
        GlobalScope.launch(Dispatchers.IO) { allProductsViewModel.deleteAll() }
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
        if (id > 0) {
            viewModel.retrieveItem(id).observe(this.viewLifecycleOwner) { selectedItem ->
                item = selectedItem
                bind(item)
            }
        } else {
            binding.saveAction.setOnClickListener {
                addNewItem()
            }
            binding.showItemWithBarcodeAction.setOnClickListener {
                showItemWithBarcode()
            }
        }

    }

    /**
     * Called before fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // Hide keyboard.
        val inputMethodManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }
}
