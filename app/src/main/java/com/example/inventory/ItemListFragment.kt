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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventory.data.Item
import com.example.inventory.databinding.ItemListFragmentBinding
import com.example.inventory.viewmodel.InventoryViewModel
import com.example.inventory.viewmodel.InventoryViewModelFactory
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class ItemListFragment : Fragment() {

    lateinit var item: Item

    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database.itemDao()
        )
    }

    private var _binding: ItemListFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemsList: List<Item>

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ItemListAdapter {
            val action =
                ItemListFragmentDirections.actionItemListFragmentToItemDetailFragment(it.id)
            this.findNavController().navigate(action)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.adapter = adapter
        viewModel.allItems.observe(this.viewLifecycleOwner) { items ->
            items.let {
                adapter.submitList(it)
            }
        }
        binding.floatingActionButton.setOnClickListener {
            val action = ItemListFragmentDirections.actionItemListFragmentToAddNewItemActivity()
            this.findNavController().navigate(action)
        }

        val sharedPreferences = this.requireActivity().getSharedPreferences("Users", Context.MODE_PRIVATE)
        val user: String? = sharedPreferences.getString("user", "nouser")

        binding.safeArgsTestText.text = user
        if (user == "Saturn" ){binding.floatingActionButton.isEnabled = false }
        binding.deleteActionButton.setOnClickListener { showExportConfirmationDialog() }
        binding.allItemsDbButton.setOnClickListener {
            val action = ItemListFragmentDirections.actionItemListFragmentToAddAllProductsFragment()
            this.findNavController().navigate(action)
        }
    }

    private fun initData() {
        viewModel.allItems.observe(this
        ) { items: List<Item> ->
            itemsList = items
        }

        viewModel.allItems.observe(this
        ) { _ ->
            viewModel.allItems.value?.let {
                itemsList = it
            }
        }
    }

    private fun exportDatabaseToCSVFile() {
        val csvFile = generateFile(requireContext(), "items.txt")
        if (csvFile != null) {
            (exportToCSVFile(csvFile))
            Toast.makeText(requireContext(), getString(R.string.csv_file_generated_text), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.csv_file_not_generated_text), Toast.LENGTH_LONG).show()
        }
    }

    private fun exportToCSVFile(csvFile: File) {
        //val sdf = SimpleDateFormat.getDateTimeInstance()
        //val currentDate = sdf.format(Date())
        csvWriter{delimiter=';'}.open(csvFile, append = false) {
            itemsList.forEachIndexed { _, item ->
                writeRow(listOf(item.itemAruid, item.itemTarolohelyid, "", item.itemMennyiseg, 0, item.itemUserid, item.itemDatum ,
                    if (!item.itemIker){
                    "False"
                }else{
                    "True"
                }, ""))
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
}
