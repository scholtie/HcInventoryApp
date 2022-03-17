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
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.inventory.data.AllProducts
import com.example.inventory.data.AllProductsDao
import com.example.inventory.data.ItemRoomDatabase
import com.example.inventory.service.CSVWriter
import kotlinx.coroutines.*
import java.io.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var navController: NavController
    /*lateinit var allItems: List<Item>
    private var shownFragment: Fragment? = null*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve NavController from the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        // Set up the action bar for use with the NavController
        //findViewById<Button>(R.id.floatingActionButton).setOnClickListener { exportCSV() }
        /*wireUpUI()*/
        title = "HCLeltar"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_list_data -> {
                deleteCurrentListData()
                true
            }
            R.id.action_re_create_database -> {
                reCreateDatabase()
                true
            }
            R.id.action_export_to_csv_file -> {
                //exportDatabaseToCSVFile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteCurrentListData() {
        AddItemFragment().removeData()
    }

    private fun reCreateDatabase() {
        GlobalScope.launch(Dispatchers.IO) {
            populateDb(ItemRoomDatabase.getDatabase(this@MainActivity))
        }
    }

    suspend fun populateDb(database: ItemRoomDatabase?) {
        database?.let { db ->
            withContext(Dispatchers.IO) {
                val allProductsDao: AllProductsDao = db.allProductsDao()

                allProductsDao.clear()

                val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "cikk.txt")
                val text = StringBuilder()
                var br: BufferedReader? = null
                val characters =
                    arrayOfNulls<String>(100000) //just an example - you have to initialize it to be big enough to hold all the lines!
                try {
                    br = BufferedReader(FileReader(file))
                    var sCurrentLine: String?
                    var i = 0
                    while (br.readLine().also { sCurrentLine = it } != null) {
                        val arr = sCurrentLine!!.split(";").toTypedArray()
                        //for the first line it'll print
                        //text.append("arr[id] = " + arr[0]) // 20000008
                        //text.append(arr[1]) // username
                        //text.append('\n')
                        val products = AllProducts(
                            productId = arr[0].toInt(),
                            productCikkszam = arr[1],
                            productCikknev = arr[3],
                            productUtbeszar = arr[5].toInt(),
                            productBrfogyar = arr[6].toInt(),
                            productEgesz = arr[8].toInt(),
                            productPlu = arr[9].toInt(),
                            productPluszekcio = arr[10].toInt(),
                            productAfaszaz = arr[11].toInt(),
                            productBrfogyar2 = arr[12].toInt(),
                            productBrfogyar3 = arr[13].toInt(),
                            productBrfogyar4 = arr[14].toInt()
                        )
                        allProductsDao.insert(products)
                        //text.append("arr[empty] = " + arr[2]) //
                        //text.append("arr[false] = " + arr[3]) // false
                        //text.append("arr[true] = " + arr[4]) // true

                        //Now if you want to enter them into separate arrays
                        characters[i] = arr[0]
                        // and you can do the same with
                        // names[1] = arr[1]
                        //etc
                        i++
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        br?.close();
                    } catch (e: IOException) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /*private fun wireUpUI()
    {
        findViewById<FloatingActionButton>(R.id.deleteActionButton).setOnClickListener { view -> exportCSV() }
    }*/
    /*private fun exportDatabaseToCSVFile() {
        val csvFile = generateFile(this, "items.csv")
        if (csvFile != null) {
            ItemListFragment().exportDirectorsToCSVFile(csvFile)
            Toast.makeText(this, getString(R.string.csv_file_generated_text), Toast.LENGTH_LONG).show()
            //val intent = goToFileIntent(this, csvFile)
            //startActivity(intent)
        } else {
            Toast.makeText(this, getString(R.string.csv_file_not_generated_text), Toast.LENGTH_LONG).show()
        }
    }*/

    private fun getCSVFileName() : String =
        "itemsdb.csv"

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