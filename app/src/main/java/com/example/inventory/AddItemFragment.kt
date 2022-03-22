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

import android.content.Context
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.inventory.data.AllProducts
import com.example.inventory.data.Item
import com.example.inventory.data.Vonalkod
import com.example.inventory.databinding.FragmentAddItemBinding
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


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
    private val vonalkodViewModel : VonalkodViewModel by activityViewModels{
        VonalkodViewModelFactory(
            (activity?.application as InventoryApplication).database.
            vonalkodDao())
    }
    private val navigationArgs: ItemDetailFragmentArgs by navArgs()

    lateinit var item: Item
    lateinit var barcode: AllProducts
    lateinit var barcodeVonalkod: Vonalkod
    private var quantity: Int = 0
    private lateinit var itemsList: List<Item>
    private lateinit var itemListAdapter: ItemListAdapter


    // Binding object instance corresponding to the fragment_add_item.xml layout
    // This property is non-null between the onCreateView() and onDestroyView() lifecycle callbacks,
    // when the view hierarchy is attached to the fragment
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

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
            binding.itemCount.text.toString()
        )
    }



    /**
     * Binds views with the passed in [item] information.
     */
    private fun bind(item: Item) {
        //val price = "%.2f".format(item.itemPrice)
        binding.apply {
            itemName.setText(item.itemAruid, TextView.BufferType.SPANNABLE)
            itemBarcode.setText(item.itemTarolohelyid, TextView.BufferType.SPANNABLE)
            itemPrice.setText(item.itemDatum.toString(), TextView.BufferType.SPANNABLE)
            itemCount.setText(item.itemMennyiseg, TextView.BufferType.SPANNABLE)
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

    private fun initData() {
        viewModel.allItems.observe(this,
            Observer { items: List<Item> ->
                itemsList = items
            }
        )

        viewModel.allItems.observe(this,
            Observer { _ ->
                // we need to refresh the movies list in case when director's name changed
                viewModel.allItems.value?.let {
                    itemsList = it
                }
            }
        )
    }

    private fun exportDatabaseToCSVFile() {
        val csvFile = generateFile(requireContext(), "items.txt")
        if (csvFile != null) {
            (exportToCSVFile(csvFile))
            Toast.makeText(requireContext(), getString(R.string.csv_file_generated_text), Toast.LENGTH_LONG).show()
            //val intent = goToFileIntent(requireContext(), csvFile)
            //startActivity(intent)
        } else {
            Toast.makeText(requireContext(), getString(R.string.csv_file_not_generated_text), Toast.LENGTH_LONG).show()
        }
    }

    fun exportToCSVFile(csvFile: File) {
        //csvWriter { delimiter= ';' }
        csvWriter{delimiter=';'}.open(csvFile, append = false) {
            // Header
            //writeRow(listOf("[id]", "[${Item.TABLE_NAME}]"))
            itemsList.forEachIndexed { _, item ->
                writeRow(listOf(item.id, item.itemAruid, item.itemUserid, item.itemMennyiseg))
            }
        }
    }

    private fun bindBarcode(barcodeVonalkod: Vonalkod) {
        binding.apply { itemName.setText(barcodeVonalkod.vonalkodAruid.toString(), TextView.BufferType.SPANNABLE)
        }
    }

    private fun bindAruid(aruid: AllProducts) {
        binding.apply { itemName.setText(aruid.productCikknev, TextView.BufferType.SPANNABLE)
        itemPrice.setText(aruid.productBrfogyar.toString(), TextView.BufferType.SPANNABLE)}
    }

    /**
     * Inserts the new Item into database and navigates up to list fragment.
     */
    private fun addNewItem() {
        val sharedPreferences = this.requireActivity().getSharedPreferences("Users", Context.MODE_PRIVATE)
        val userId: String? = sharedPreferences.getString("id", "0")
        if (isEntryValid()) {
            val barcodeValue = binding.itemBarcode.text.toString()
            vonalkodViewModel.retrieveMatchingAruid(barcodeValue)
                .observe(this.viewLifecycleOwner) { barcodeTest ->
                    try {
                        barcodeVonalkod = barcodeTest
                        allProductsViewModel.retrieveMatchingBarcode(barcodeVonalkod.vonalkodAruid)
                            .observe(this.viewLifecycleOwner) { aruid ->
                                try {
                                    barcode = aruid
                                    viewModel.addNewItem(
                                        barcode.productId,
                                        binding.itemCount.text.toString().toInt(),
                                        10.0,
                                        "testTarolohely",
                                        userId!!.toInt(),
                                        false
                                    )
                                    val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
                                    findNavController().navigate(action)
                                }
                                catch (e: Exception){
                                    println("Nem talált termék")

                                }
                            }
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
                42341,
                binding.itemCount.text.toString().toInt(),
                10.0,
                "testTarolohely",
                2,
                false
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
            vonalkodViewModel.retrieveMatchingAruid(barcodeValue)
                .observe(this.viewLifecycleOwner) { barcodeTest ->
                    try {
                        barcodeVonalkod = barcodeTest
                        allProductsViewModel.retrieveMatchingBarcode(barcodeVonalkod.vonalkodAruid)
                            .observe(this.viewLifecycleOwner) { aruid ->
                                try {
                                    barcode = aruid
                                    bindAruid(barcode)
                                }
                                catch (e: Exception){
                                    println("Nem talált termék")

                                }                                }
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
        //val productBarcode = binding.itemBarcode.text.toString()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage("Nem található a vonalkód az adatbázisban.")
            .setCancelable(false)
            .setNeutralButton("Ok"){ _, _ -> }
            /*.setNegativeButton("Nem") { _, _ -> }
            .setPositiveButton("Igen") { _, _ -> val action =
                AddItemFragmentDirections.actionAddItemFragmentToAddAllProductsFragment(0, productBarcode)
                findNavController().navigate(action)
            }*/
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
            binding.btnExportCSV.setOnClickListener { exportDatabaseToCSVFile() }
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
