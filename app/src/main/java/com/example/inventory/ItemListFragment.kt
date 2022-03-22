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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventory.data.Item
import com.example.inventory.databinding.ItemListFragmentBinding
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Main fragment displaying details for all items in the database.
 */
class ItemListFragment : Fragment() {

    lateinit var item: Item
    //private lateinit var inventoryViewModel: InventoryViewModel
    private lateinit var allItems: List<Item>
    private val args:ItemListFragmentArgs by navArgs()
    //abstract val allItems:List<Item>

    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database.itemDao()
        )
    }

    private var _binding: ItemListFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemsList: List<Item>

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }*/

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initData()
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemListFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

   /*private fun initData() {
       }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myUser = args.user
        val adapter = ItemListAdapter {
            val action =
                ItemListFragmentDirections.actionItemListFragmentToItemDetailFragment(it.id)
            this.findNavController().navigate(action)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.adapter = adapter
        // Attach an observer on the allItems list to update the UI automatically when the data
        // changes.
        /*viewModel.allItems.observe(this.viewLifecycleOwner
        ) { items: List<Item> ->
            allItems = items
        }*/

        viewModel.allItems.observe(this.viewLifecycleOwner) { items ->
            items.let {
                adapter.submitList(it)
            }
        }



        //binding.floatingActionButton2.setOnClickListener { /*exportDatabaseToCSVFile()*/ }
        binding.floatingActionButton.setOnClickListener {
            val action = ItemListFragmentDirections.actionItemListFragmentToAddItemFragment(
                getString(R.string.add_fragment_title)
            )
            this.findNavController().navigate(action)
        }
        binding.barcodeTestActionButton.setOnClickListener {
            val action = ItemListFragmentDirections.actionItemListFragmentToTestActivity()
            this.findNavController().navigate(action)
        }
        /*binding.textInputTestActionButton.setOnClickListener {
            val action = ItemListFragmentDirections.actionItemListFragmentToTextInputTest()
            this.findNavController().navigate(action)
        }*/


        /*binding.deleteActionButton.setOnClickListener {
            showExportConfirmationDialog()
        }*/

        val sharedPreferences = this.requireActivity().getSharedPreferences("Users", Context.MODE_PRIVATE)
        val user: String? = sharedPreferences.getString("user", "nouser")
        val userId: String? = sharedPreferences.getString("id", "noId")

        binding.safeArgsTestText.setText(user)
        if (user == "Saturn" ){binding.floatingActionButton.isEnabled = false }
        binding.deleteActionButton.setOnClickListener { showExportConfirmationDialog() }
        binding.allItemsDbButton.setOnClickListener {
            val action = ItemListFragmentDirections.actionItemListFragmentToAddAllProductsFragment()
            this.findNavController().navigate(action)
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
        val sdf = SimpleDateFormat("dd/M/yyyy")
        val currentDate = sdf.format(Date())
        csvWriter{delimiter=';'}.open(csvFile, append = false) {
            // Header
            //writeRow(listOf("[id]", "[${Item.TABLE_NAME}]"))
            itemsList.forEachIndexed { _, item ->
                writeRow(listOf(item.itemAruid, item.itemTarolohelyid ,item.itemMennyiseg, item.itemUserid, item.itemDatum , item.itemIker))
            }
        }

    }


    private fun showExportConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage(getString(R.string.export_question))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ -> exportDatabaseToCSVFile()
            }
            .show()
    }


    private fun nukeTable() {
        findNavController().navigateUp()
    }
    /*private fun exportCSV(){
        val database: ItemRoomDatabase by lazy { ItemRoomDatabase.getDatabase(requireActivity()) }
        val exportDir = File(Environment.DIRECTORY_DOWNLOADS)// your path where you want save your file
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val file = File(exportDir, "item.csv")//$TABLE_NAME.csv is like user.csv or any name you want to save
        try {
            file.createNewFile()
            val csvWrite = CSVWriter(FileWriter(file))
            val curCSV = database.query("SELECT * FROM item", null)// query for get all data of your database table
            csvWrite.writeNext(curCSV.columnNames)
            while (curCSV.moveToNext()) {
                //Which column you want to export
                val arrStr = arrayOfNulls<String>(curCSV.columnCount)
                for (i in 0 until curCSV.columnCount - 1) {
                    when (i) {
                        20, 22 -> {
                        }
                        else -> arrStr[i] = curCSV.getString(i)
                    }
                }
                csvWrite.writeNext(arrStr)
            }
            csvWrite.close()
            curCSV.close()
        } catch (sqlEx: Exception) {
            //Timber.e(sqlEx)
        }

    }
    fun exportDatabase(){
        val sd = Environment.getExternalStorageDirectory()

        // Get the Room database storage path using SupportSQLiteOpenHelper
        ItemRoomDatabase.getDatabase(requireActivity()).openHelper.writableDatabase.path

        if (sd.canWrite()) {
            val currentDBPath = ItemRoomDatabase.getDatabase(requireActivity()).openHelper.writableDatabase.path
            val backupDBPath = "mydb.csv"      //you can modify the file type you need to export
            val currentDB = File(currentDBPath)
            val backupDB = File(sd, backupDBPath)
            if (currentDB.exists()) {
                try {
                    val src = FileInputStream(currentDB).channel
                    val dst = FileOutputStream(backupDB).channel
                    dst.transferFrom(src, 0, src.size())
                    src.close()
                    dst.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }*/

    /*private fun exportDatabaseToCSVFile() {
        val csvFile = generateFile(requireActivity(), getCSVFileName())
        if (csvFile != null) {
            exportDirectorsToCSVFile(csvFile)
            Toast.makeText(requireActivity(), getString(R.string.csv_file_generated_text), Toast.LENGTH_LONG).show()
            val intent = goToFileIntent(requireActivity(), csvFile)
            startActivity(intent)
        } else {
            Toast.makeText(requireActivity(), getString(R.string.csv_file_not_generated_text), Toast.LENGTH_LONG).show()
        }
    }

    private fun getCSVFileName() : String =
        "itemsdb.csv"*/

    /*private fun exportDatabaseToCSVFile() {
        val csvFile = generateFile(requireContext(), "items.csv")
        if (csvFile != null) {
            ItemListFragment().exportDirectorsToCSVFile(csvFile)
            Toast.makeText(requireContext(), getString(R.string.csv_file_generated_text), Toast.LENGTH_LONG).show()
            //val intent = goToFileIntent(this, csvFile)
            //startActivity(intent)
        } else {
            Toast.makeText(requireContext(), getString(R.string.csv_file_not_generated_text), Toast.LENGTH_LONG).show()
        }
    }*/

    /*fun exportDirectorsToCSVFile(csvFile: File) {
        viewModel.allItems.observe(this.viewLifecycleOwner) { items ->
            items.let {
                allItems = items
            }
        }

        csvWriter().open(csvFile, append = false) {
            // Header
            writeRow(listOf("[id]", "Name", "Barcode", "Price", "Quantity"))
            allItems.forEachIndexed{ index, item ->
                writeRow(listOf(index, item.itemName, item.itemBarcode, item.itemPrice, item.quantityInStock))
            }
        }
    }*/

}
