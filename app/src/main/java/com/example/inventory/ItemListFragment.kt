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

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventory.data.Item
import com.example.inventory.data.ItemRoomDatabase
import com.example.inventory.databinding.ItemListFragmentBinding
import com.example.inventory.service.MyFTPClientFunctions
import com.example.inventory.service.NetworkAvailable
import com.example.inventory.viewmodel.InventoryViewModel
import com.example.inventory.viewmodel.InventoryViewModelFactory
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class ItemListFragment : Fragment() {

    lateinit var item: Item
    private var ftpclient: MyFTPClientFunctions? = null
    private var networkAvailable: NetworkAvailable? = null

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
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
                lifecycleScope.launch {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(android.R.string.dialog_alert_title))
                            .setMessage(getString(R.string.logout_question))
                            .setCancelable(true)
                            .setNegativeButton(getString(R.string.no)) { _, _ -> }
                            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                val sharedPreferences = requireActivity()
                                .getSharedPreferences("Users", Context.MODE_PRIVATE)
                                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                                editor.putString("user", "")
                                editor.putString("id", "")
                                editor.apply()
                                val switchActivityIntent = Intent(requireContext(),
                                    LoginActivity::class.java)
                                startActivity(switchActivityIntent)
                            }
                            .show()
                }

        }
        callback.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemListFragmentBinding.inflate(inflater, container, false)
        ftpclient = MyFTPClientFunctions()
        networkAvailable = NetworkAvailable()
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

        val sharedPreferences = this.requireActivity().
        getSharedPreferences("Users", Context.MODE_PRIVATE)
        val user: String? = sharedPreferences.getString("user", "nouser")

        binding.safeArgsTestText.text = user
        //if (user == "Saturn" ){binding.floatingActionButton.isEnabled = false }
        binding.deleteActionButton.setOnClickListener { showExportConfirmationDialog() }
        /*binding.allItemsDbButton.setOnClickListener {
            val action = ItemListFragmentDirections.actionItemListFragmentToAddAllProductsFragment()
            this.findNavController().navigate(action)
        }*/
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
        //removeFtp()
        val sharedPreferencesSaveDate = activity?.getSharedPreferences("SaveDate", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor =
            sharedPreferencesSaveDate!!.edit()
        val sdf = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
        val currentDate = sdf.format(Date()).toString()
        editor.putString(
            "datum",
            currentDate
        )
        editor.apply()
        val currentDateShared: String? = sharedPreferencesSaveDate.getString("datum", "")
        binding.deleteActionButton.isEnabled = false
        val csvFile = generateFile(requireContext(), "leltar$currentDateShared.txt")
        if (csvFile != null) {
            (exportToCSVFile(csvFile))
            Toast.makeText(requireContext(), getString(R.string.csv_file_generated_text), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.csv_file_not_generated_text), Toast.LENGTH_LONG).show()
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun exportToCSVFile(csvFile: File) {
        //val sdf = SimpleDateFormat.getDateTimeInstance()
        //val currentDate = sdf.format(Date())
        csvWriter{delimiter=';'}.open(csvFile, append = false) {
            itemsList.forEachIndexed { _, item ->
                writeRow(listOf(item.itemAruid, item.itemCikkszam, item.itemTarolohelyid, "", item.itemMennyiseg, 0,
                    item.itemUserid, item.itemDatum ,
                    if (!item.itemIker){
                    "False"
                }else{
                    "True"
                }, ""))
            }
        }

        val path = requireContext().
        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        Log.d("Files", "Path: $path")
        val directory = File(path)
        val files = directory.listFiles()
        if (files != null) {
            Log.d("Files", "Size: " + files.size)
        }
        for (i in files!!.indices) {
            Log.d("Files", "FileName:" + files[i].name)
            val diff: Long = Date().time - files[i].lastModified()
            val cutoff: Long = 24 * (24 * 60 * 60 * 1000)

            if (files[i].name.startsWith("leltar"))
            {
                if (diff > cutoff) {
                    files[i].delete()
                }
            }
        }

        if (networkAvailable!!.isOnline(requireContext()))
        {connectFtp()}
        else{
            Toast.makeText(requireContext(), "Nincs internetkapcsolat",
                Toast.LENGTH_SHORT).show()

            activity?.runOnUiThread { binding.deleteActionButton.isEnabled = true
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)}
        }

    }

    private fun connectFtp(){
        val sharedPreferencesFtp = this.requireActivity().
        getSharedPreferences("FtpDetails", Context.MODE_PRIVATE)
        val srcFilePath: String? = sharedPreferencesFtp.getString("path", "")
        val username: String? = sharedPreferencesFtp.getString("username", "")
        val password: String? = sharedPreferencesFtp.getString("password", "")
        val host: String? = sharedPreferencesFtp.getString("host", "")
        val port: String? = sharedPreferencesFtp.getString("port", "0")
        Thread {
            // host ??? your FTP address
            // username & password ??? for your secured login
            // 21 default gateway for FTP
            val status: Boolean = ftpclient!!.ftpConnect(host!!, username, password, port!!.toInt())
            val directoryExists: Boolean = ftpclient!!.ftpChangeDirectory(srcFilePath!!)
            if (status) {
                Log.d(ContentValues.TAG, "Connection Success")
                if (directoryExists)
                {
                    ftpclient!!.ftpChangeDirectory(srcFilePath)
                    uploadFtp()
                    //ftpclient!!.ftpPrintFilesList(srcFilePath)
                    activity?.runOnUiThread { binding.deleteActionButton.isEnabled = true }
                }
                else{
                    Log.d(ContentValues.TAG, "Upload failed")
                    Toast.makeText(requireContext(), "Felt??lt??s sikertelen, el??r??si ??t nem l??tezik",
                        Toast.LENGTH_SHORT).show()
                    activity?.runOnUiThread {
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        binding.deleteActionButton.isEnabled = true }
                }
            } else {
                Log.d(ContentValues.TAG, "Connection failed")
                Toast.makeText(requireContext(), "Felt??lt??s sikertelen",
                    Toast.LENGTH_SHORT).show()
                activity?.runOnUiThread { binding.deleteActionButton.isEnabled = true }
            }
        }.start()
    }

    private fun uploadFtp(){
        val sharedPreferencesFtp = this.requireActivity().
        getSharedPreferences("FtpDetails", Context.MODE_PRIVATE)
        val sharedPreferencesSaveDate = activity?.getSharedPreferences("SaveDate", Context.MODE_PRIVATE)
        val srcFilePath: String? = sharedPreferencesFtp.getString("path", "")
        //val sdf = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
        //val currentDate = sdf.format(Date()).toString()
        val currentDateShared: String? = sharedPreferencesSaveDate!!.getString("datum", "")
        println()
        val desFilePath = requireContext().
        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()

        ftpclient!!.ftpUpload("$desFilePath/leltar$currentDateShared.txt",
            "leltar$currentDateShared.txt", srcFilePath, requireContext())

        val lastFileSharedPreferences = this.requireActivity()
            .getSharedPreferences("LastFile", Context.MODE_PRIVATE)
        val deleteFilePath: String? = lastFileSharedPreferences.getString("fileName", "")
        if (deleteFilePath != "")
        {
            ftpclient!!.ftpRemoveFile("$srcFilePath/$deleteFilePath")
        }
        val editor: SharedPreferences.Editor = lastFileSharedPreferences.edit()
        editor.putString("fileName", "leltar$currentDateShared.txt")
        editor.apply()


        requireActivity().runOnUiThread { Toast.makeText(requireContext(),
            "Sikeresen felt??ltve a szerverre", Toast.LENGTH_SHORT).show()
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)}

    }

    private fun removeFtp(){
        val sharedPreferencesFtp = this.requireActivity().
        getSharedPreferences("FtpDetails", Context.MODE_PRIVATE)
        val srcFilePath: String? = sharedPreferencesFtp.getString("path", "")
        //val sharedPreferencesFileName = this.requireActivity(). getSharedPreferences("LastFile", Context.MODE_PRIVATE)
        //val deleteFilePath: String? = sharedPreferencesFileName.getString("fileName", "")
        println(srcFilePath)
        Thread{
            ftpclient!!.ftpRemoveFile("$srcFilePath/leltar2022.04.29.09.18.15.txt")
        }
    }


    private fun showExportConfirmationDialog() {
        lifecycleScope.launch {
            val itemList: List<Item> =
                ItemRoomDatabase.getDatabase(requireContext()).itemDao().getAll()
            if (itemList.isNotEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(android.R.string.dialog_alert_title))
                    .setMessage(getString(R.string.export_question))
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.no)) { _, _ -> }
                    .setPositiveButton(getString(R.string.yes)) { _, _ -> exportDatabaseToCSVFile()
                        requireActivity().window.setFlags(
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }
                    .show()
            } else {
                Toast.makeText(requireContext(), "M??g nincs hozz??adva term??k!",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}
