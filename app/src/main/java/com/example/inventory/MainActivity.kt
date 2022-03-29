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

import android.R.attr.host
import android.R.attr.password
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.fragment.NavHostFragment
import com.example.inventory.data.*
import com.example.inventory.service.MyFTPClientFunctions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var navController: NavController
    private var ftpclient: MyFTPClientFunctions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        findViewById<Button>(R.id.btnFtpTest).setOnClickListener { connectFtp() }
        title = "HCLeltar"
        ftpclient = MyFTPClientFunctions()
        //DWUtilities.CreateDWProfile(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navController.navigate(R.id.addNewItemActivity)
    }

    private fun connectFtp(){
        val username = "test"
        val password = "test"
        val host = "ftp.pointer.hu"
        Thread {
            // host – your FTP address
            // username & password – for your secured login
            // 21 default gateway for FTP
            val status: Boolean = ftpclient!!.ftpConnect(host, username, password, 21)
            if (status) {
                Log.d(TAG, "Connection Success")
            } else {
                Log.d(TAG, "Connection failed")
            }
        }.start()
    }

    /*private fun displayScanResult(scanIntent: Intent) {
        val decodedSource =
            scanIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_source))
        val decodedData =
            scanIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_data))
        val decodedLabelType =
            scanIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_label_type))
        val scan = "$decodedData"

    }*/

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_list_data -> {
                showDeleteConfirmationDialog()
                true
            }
            R.id.action_re_create_database -> {
                showConfirmationDialog()
                true
            }
            R.id.action_export_to_csv_file -> {
                //shownFragment as? AddItemFragment)?.showItemWithBarcode()
                /*val fm: FragmentManager = supportFragmentManager
                val fragment: AddItemFragment? =
                    fm.findFragmentById(R.id.addItemFragment) as? AddItemFragment
                fragment?.showItemWithBarcode()*/
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private suspend fun deleteCurrentListData(database: ItemRoomDatabase?) {
        database?.let { db ->
            withContext(Dispatchers.IO) {
                val itemDao: ItemDao = db.itemDao()

                itemDao.clear()
            }
        }
    }

    private suspend fun fetchDocs() =
        coroutineScope {
            val deferredOne =
                async { populateDbAllProducts(ItemRoomDatabase.getDatabase(this@MainActivity)) }
            val deferredTwo =
                async { populateDbUserek(ItemRoomDatabase.getDatabase(this@MainActivity)) }
            val deferredThree =
                async { populateDbVonalkodok(ItemRoomDatabase.getDatabase(this@MainActivity)) }
            val deferredFour =
                async {populateDbLeltarhely(ItemRoomDatabase.getDatabase(this@MainActivity))}
            deferredOne.await()
            deferredTwo.await()
            deferredThree.await()
            deferredFour.await()
        }

    private suspend fun populateDbAllProducts(database: ItemRoomDatabase?) {
        database?.let { db ->
            withContext(Dispatchers.IO) {
                val allProductsDao: AllProductsDao = db.allProductsDao()

                allProductsDao.clear()

                val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "cikk.txt")
                var br: BufferedReader? = null
                val characters =
                    arrayOfNulls<String>(100000) //just an example - you have to initialize it to be big enough to hold all the lines!
                try {
                    br = BufferedReader(FileReader(file))
                    var sCurrentLine: String?
                    var i = 0
                    while (br.readLine().also { sCurrentLine = it } != null) {
                        val arr = sCurrentLine!!.split(";").toTypedArray()
                        val products = AllProducts(
                            productId = arr[0].toInt(),
                            productCikkszam = arr[1],
                            productCikknev = arr[3],
                            productUtbeszar = arr[5].toInt(),
                            productBrfogyar = arr[7].toInt(),
                            productEgesz = arr[8].toInt(),
                            productPlu = arr[9].toInt(),
                            productPluszekcio = arr[10].toInt(),
                            productAfaszaz = arr[11].toInt(),
                            productBrfogyar2 = arr[12].toInt(),
                            productBrfogyar3 = arr[13].toInt(),
                            productBrfogyar4 = arr[14].toInt()
                        )
                        allProductsDao.insert(products)
                        println(products)
                        characters[i] = arr[0]
                        i++
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread { Toast.makeText(this@MainActivity, "cikk.txt fájl nem találva", Toast.LENGTH_SHORT).show() }
                } finally {
                    try {
                        br?.close()
                        println("Termékek beolvasva")
                        runOnUiThread { Toast.makeText(this@MainActivity, "cikk.txt beolvasva", Toast.LENGTH_SHORT).show() }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /*private fun ftpTest(){
        val ftpClient = FTPClient()
        ftpClient.connect(InetAddress.getByName(server))
        ftpClient.login(user, password)
        ftpClient.changeWorkingDirectory(serverRoad)
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE)

        var buffIn: BufferedInputStream? = null
        buffIn = BufferedInputStream(FileInputStream(file))
        ftpClient.enterLocalPassiveMode()
        ftpClient.storeFile("test.txt", buffIn)
        buffIn.close()
        ftpClient.logout()
        ftpClient.disconnect()
    }*/

    private suspend fun populateDbVonalkodok(database: ItemRoomDatabase?) {
        database?.let { db ->
            withContext(Dispatchers.IO) {
                val vonalkodDao: VonalkodDao = db.vonalkodDao()

                vonalkodDao.clear()

                val fileVk =
                    File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "vonalkod.txt")
                var brVk: BufferedReader? = null
                val charactersVk =
                    arrayOfNulls<String>(200000) //just an example - you have to initialize it to be big enough to hold all the lines!
                try {
                    brVk = BufferedReader(FileReader(fileVk))
                    var sCurrentLine: String?
                    var i = 0

                    while (brVk.readLine().also { sCurrentLine = it } != null) {
                        val arr = sCurrentLine!!.split(";").toTypedArray()
                        val vonalkodok = Vonalkod(
                            vonalkodAruid = arr[0].toInt(),
                            vonalkodBarcode = arr[1],
                            vonalkodKarton = arr[2].toInt(),
                        )
                        vonalkodDao.insert(vonalkodok)
                        println(vonalkodok)
                        charactersVk[i] = arr[0]
                        findViewById<ProgressBar>(R.id.progressBar).isVisible = true
                        i++
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread { Toast.makeText(this@MainActivity, "vonalkod.txt fájl nem találva", Toast.LENGTH_SHORT).show() }
                } finally {
                    try {
                        brVk?.close()
                        println("Vonalkódok beolvasva")
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        runOnUiThread {
                            findViewById<ProgressBar>(R.id.progressBar).isVisible = false
                            Toast.makeText(this@MainActivity, "vonalkod.txt beolvasva", Toast.LENGTH_SHORT).show()
                            val pendingIntent: PendingIntent =
                                NavDeepLinkBuilder(this@MainActivity)
                                    .setGraph(R.navigation.nav_graph)
                                    .setDestination(R.id.loginFragment)
                                    .createPendingIntent()

                            try {
                                pendingIntent.send()
                            } catch (e: PendingIntent.CanceledException) {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private suspend fun populateDbUserek(database: ItemRoomDatabase?) {
        database?.let { db ->
            withContext(Dispatchers.IO) {
                val usersDao: UsersDao = db.usersDao()

                usersDao.clear()

                val fileUs =
                    File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ugyintezo.txt")
                var brUs: BufferedReader? = null
                val charactersUs =
                    arrayOfNulls<String>(50) //just an example - you have to initialize it to be big enough to hold all the lines!
                try {
                    brUs = BufferedReader(FileReader(fileUs))
                    var sCurrentLine: String?
                    var i = 0
                    while (brUs.readLine().also { sCurrentLine = it } != null) {
                        val arr = sCurrentLine!!.split(";").toTypedArray()
                        val users = Users(
                            userId = arr[0].toInt(),
                            userName = arr[1],
                            userPass = arr[2],
                        )
                        usersDao.insert(users)
                        println(users)
                        charactersUs[i] = arr[0]
                        i++
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread { Toast.makeText(this@MainActivity, "ugyintezo.txt fájl nem találva", Toast.LENGTH_SHORT).show() }
                } finally {
                    try {
                        brUs?.close()
                        println("Userek beolvasva")
                        runOnUiThread { Toast.makeText(this@MainActivity, "ugyintezo.txt beolvasva", Toast.LENGTH_SHORT).show() }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private suspend fun populateDbLeltarhely(database: ItemRoomDatabase?) {
        database?.let { db ->
            withContext(Dispatchers.IO) {
                val leltarhelyDao: LeltarhelyDao = db.leltarhelyDao()

                leltarhelyDao.clear()

                val fileUs =
                    File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "leltarhely.txt")
                var brUs: BufferedReader? = null
                val charactersUs =
                    arrayOfNulls<String>(10) //just an example - you have to initialize it to be big enough to hold all the lines!
                try {
                    brUs = BufferedReader(FileReader(fileUs))
                    var sCurrentLine: String?
                    var i = 0
                    while (brUs.readLine().also { sCurrentLine = it } != null) {
                        val arr = sCurrentLine!!.split(";").toTypedArray()
                        val leltarhely = Leltarhely(
                            leltarhelyId = arr[0].toInt(),
                            leltarhelyName = arr[1]
                        )
                        leltarhelyDao.insert(leltarhely)
                        println(leltarhely)
                        charactersUs[i] = arr[0]
                        i++
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread { Toast.makeText(this@MainActivity, "leltarhely.txt fájl nem találva", Toast.LENGTH_SHORT).show() }
                } finally {
                    try {
                        brUs?.close()
                        println("Leltarhely beolvasva")
                        runOnUiThread { Toast.makeText(this@MainActivity, "leltarhely.txt beolvasva", Toast.LENGTH_SHORT).show() }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage("Adatbázis újraírása a fájlokból. Biztosan folytatni szeretné?")
            .setCancelable(false)
            .setNegativeButton("Nem") { _, _ -> }
            .setPositiveButton("Igen") { _, _ ->
                GlobalScope.launch(Dispatchers.IO) { fetchDocs() }
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                findViewById<ProgressBar>(R.id.progressBar).isVisible = true
            }
            .show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage(getString(R.string.DeleteConfirmation))
            .setCancelable(false)
            .setNegativeButton(R.string.no) { _, _ -> }
            .setPositiveButton(R.string.yes) { _, _ ->
                GlobalScope.launch(Dispatchers.IO){deleteCurrentListData(ItemRoomDatabase.getDatabase(this@MainActivity))}
            }
            .show()
    }

    /**
     * Handle navigation when the user chooses Up from the action bar.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}