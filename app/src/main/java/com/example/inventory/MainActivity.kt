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

import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.example.inventory.data.ItemRoomDatabase
import com.example.inventory.extension.showToast
import com.example.inventory.service.CSVWriter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import kotlin.math.exp

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve NavController from the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        // Set up the action bar for use with the NavController
        //findViewById<Button>(R.id.floatingActionButton).setOnClickListener { exportCSV() }
        /*wireUpUI()*/
    }

    /*private fun wireUpUI()
    {
        findViewById<FloatingActionButton>(R.id.deleteActionButton).setOnClickListener { view -> exportCSV() }
    }*/

    private fun exportCSV(){
        val database: ItemRoomDatabase by lazy { ItemRoomDatabase.getDatabase(this) }
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

    /* private fun getCSVFileName() : String =
         "ItemRoomExample.csv"
     private fun exportDatabaseToCSVFile() {
         val csvFile = generateFile(this, getCSVFileName())
         if (csvFile != null) {
                 (shownFragment as DirectorsListFragment).exportDirectorsToCSVFile(csvFile)
             Toast.makeText(this, getString(R.string.csv_file_generated_text), Toast.LENGTH_LONG).show()
             val intent = goToFileIntent(this, csvFile)
             startActivity(intent)
         } else {
             Toast.makeText(this, getString(R.string.csv_file_not_generated_text), Toast.LENGTH_LONG).show()
         }
     }*/


    /**
     * Handle navigation when the user chooses Up from the action bar.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}