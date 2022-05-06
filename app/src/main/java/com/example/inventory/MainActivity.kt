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

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.inventory.data.*
import com.example.inventory.service.MyFTPClientFunctions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var navController: NavController
    private var ftpclient: MyFTPClientFunctions? = null
    private var version65OrOver = false
    val storage = Firebase.storage
    val storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        //findViewById<Button>(R.id.btnFtpTest).setOnClickListener { connectFtp() }
        title = "HCLeltar"
        ftpclient = MyFTPClientFunctions()
        //DWUtilities.CreateDWProfile(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navController.navigate(R.id.addNewItemActivity)
    }

    private fun connectFtp(){
        val sharedPreferencesFtp = this.getSharedPreferences(
            "FtpDetails", Context.MODE_PRIVATE)
        val srcFilePath: String? = sharedPreferencesFtp.getString("pathSend", "")
        val username: String? = sharedPreferencesFtp.getString("username", "")
        val password: String? = sharedPreferencesFtp.getString("password", "")
        val host: String? = sharedPreferencesFtp.getString("host", "")
        val port: String? = sharedPreferencesFtp.getString("port", "0")
        Thread {
            // host – your FTP address
            // username & password – for your secured login
            // 21 default gateway for FTP
            val status: Boolean = ftpclient!!.ftpConnect(host!!, username, password, port!!.toInt())
            if (status) {
                Log.d(TAG, "Connection Success")
                ftpclient!!.ftpChangeDirectory(srcFilePath!!)
                val areThereFiles: Array<String?>? = ftpclient!!.ftpPrintFilesList(srcFilePath)
                if (areThereFiles!!.isNotEmpty())
                {
                    downloadFtp()
                }
                else{
                    runOnUiThread {
                        findViewById<ProgressBar>(R.id.progressBar).isVisible =
                            false
                        Toast.makeText(
                            this,
                            getString(R.string.állományoknemtalálh),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Log.d(TAG, "Connection failed")
                GlobalScope.launch(Dispatchers.IO) { fetchDocs() }
            }
        }.start()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun downloadFtp(){
        val sharedPreferencesFtp = this.getSharedPreferences(
            "FtpDetails", Context.MODE_PRIVATE)
        val srcFilePath: String? = sharedPreferencesFtp.getString("pathSend", "")
        val srcFileNameCikk = "cikk.txt"
        val srcFileNameVonalkod = "vonalkod.txt"
        val srcFileNameLeltarhely = "leltarhely.txt"
        val srcFileNameLeltarfej = "leltarfej.txt"
        val srcFileNameUgyintezo = "ugyintezo.txt"
        val desFilePath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()
        val desFileP = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        try{
            val fileCikk = File(desFileP, srcFileNameCikk)
            fileCikk.delete()
            val fileVonalkod = File(desFileP, srcFileNameVonalkod)
            fileVonalkod.delete()
            val fileLelarhely = File(desFileP, srcFileNameLeltarhely)
            fileLelarhely.delete()
            val fileLeltarfej = File(desFileP, srcFileNameLeltarfej)
            fileLeltarfej.delete()
            val fileUgyintezo = File(desFileP, srcFileNameUgyintezo)
            fileUgyintezo.delete()
            try {
                ftpclient!!.ftpDownload("$srcFilePath/$srcFileNameCikk",
                    "$desFilePath/$srcFileNameCikk"
                )
                ftpclient!!.ftpDownload("$srcFilePath/$srcFileNameVonalkod",
                    "$desFilePath/$srcFileNameVonalkod"
                )
                ftpclient!!.ftpDownload("$srcFilePath/$srcFileNameLeltarhely",
                    "$desFilePath/$srcFileNameLeltarhely"
                )
                ftpclient!!.ftpDownload("$srcFilePath/$srcFileNameLeltarfej",
                    "$desFilePath/$srcFileNameLeltarfej"
                )
                ftpclient!!.ftpDownload("$srcFilePath/$srcFileNameUgyintezo",
                    "$desFilePath/$srcFileNameUgyintezo"
                )
                GlobalScope.launch(Dispatchers.IO) { fetchDocs()}
            }catch(e: Exception){
                e.printStackTrace()
                runOnUiThread {
                        findViewById<ProgressBar>(R.id.progressBar).isVisible =
                            false
                        Toast.makeText(
                            this,
                            getString(R.string.állományoknemtalálh),
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
            finally {

            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                findViewById<ProgressBar>(R.id.progressBar).isVisible = false }
        }
        finally {
            ftpclient!!.ftpRemoveFile("$srcFilePath/$srcFileNameCikk")
            ftpclient!!.ftpRemoveFile("$srcFilePath/$srcFileNameVonalkod")
            ftpclient!!.ftpRemoveFile("$srcFilePath/$srcFileNameLeltarhely")
            ftpclient!!.ftpRemoveFile("$srcFilePath/$srcFileNameLeltarfej")
            ftpclient!!.ftpRemoveFile("$srcFilePath/$srcFileNameUgyintezo")

        }

    }

    private fun disconnectFtp(){
        Thread{
            val status : Boolean = ftpclient!!.ftpDisconnect()
            if (status) {
                Log.d(TAG, "Disconnection Success")
            } else {
                Log.d(TAG, "Disconnection failed")
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

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_db_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            R.id.action_db_import -> {
                showConfirmationDialog()
                true
            }
            R.id.action_ftpsettings -> {
                val switchActivityIntent = Intent(this, FtpLoginActivity::class.java)
                startActivity(switchActivityIntent)
                true
            }
            R.id.action_scannersettings -> {
                val switchActivityIntent = Intent(this, ScannerSettingsActivity::class.java)
                switchActivityIntent.putExtra(ScannerSettingsActivity.SETTINGS_KEY_VERSION, version65OrOver)
                startActivity(switchActivityIntent)
                true
            }
            R.id.action_logout -> {
                val sharedPreferences = this
                    .getSharedPreferences("Users", Context.MODE_PRIVATE)
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("user", "")
                editor.putString("id", "")
                editor.apply()
                val switchActivityIntent = Intent(this, LoginActivity::class.java)
                startActivity(switchActivityIntent)
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
            val db = ItemRoomDatabase.getDatabase(this@MainActivity)
            val deferredOne =
                async { populateDbAllProducts(
                    db) }
            val deferredTwo =
                async { populateDbUserek(
                    db) }
            val deferredThree =
                async { populateDbVonalkodok(
                    db) }
            val deferredFour =
                async {populateDbLeltarhely(
                    db)}
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

                val file = File(getExternalFilesDir(
                    Environment.DIRECTORY_DOCUMENTS), "cikk.txt")
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
                    runOnUiThread {
                        Toast.makeText(this@MainActivity,
                            "cikk.txt fájl nem létezik", Toast.LENGTH_SHORT).show() }
                } finally {
                    try {
                        br?.close()
                        println("Termékek beolvasva")
                        runOnUiThread {
                            Toast.makeText(this@MainActivity,
                                "cikk.txt beolvasva", Toast.LENGTH_SHORT).show() }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private suspend fun populateDbVonalkodok(database: ItemRoomDatabase?) {
        database?.let { db ->
            withContext(Dispatchers.IO) {
                val vonalkodDao: VonalkodDao = db.vonalkodDao()

                vonalkodDao.clear()

                val fileVk =
                    File(getExternalFilesDir(
                        Environment.DIRECTORY_DOCUMENTS), "vonalkod.txt")
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
                    runOnUiThread {
                        Toast.makeText(this@MainActivity,
                            "vonalkod.txt fájl nem létezik", Toast.LENGTH_SHORT).show() }
                } finally {
                    try {
                        brVk?.close()
                        println("Vonalkódok beolvasva")
                        runOnUiThread {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            findViewById<ProgressBar>(R.id.progressBar).isVisible = false
                            Toast.makeText(this@MainActivity,
                                "vonalkod.txt beolvasva", Toast.LENGTH_SHORT).show()
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
                    File(getExternalFilesDir(
                        Environment.DIRECTORY_DOCUMENTS), "ugyintezo.txt")
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
                    runOnUiThread {
                        Toast.makeText(this@MainActivity,
                            "ugyintezo.txt fájl nem létezik", Toast.LENGTH_SHORT).show() }
                } finally {
                    try {
                        brUs?.close()
                        println("Userek beolvasva")
                        runOnUiThread {
                            Toast.makeText(this@MainActivity,
                                "ugyintezo.txt beolvasva", Toast.LENGTH_SHORT).show() }
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
                val sharedPreferencesIker = this@MainActivity.getSharedPreferences(
                    "IkerRaktar", Context.MODE_PRIVATE)
                leltarhelyDao.clear()

                val fileUs =
                    File(getExternalFilesDir(
                        Environment.DIRECTORY_DOCUMENTS), "leltarhely.txt")
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
                    runOnUiThread {
                        Toast.makeText(this@MainActivity,
                            "leltarhely.txt fájl nem létezik", Toast.LENGTH_SHORT).show() }
                } finally {
                    try {
                        brUs?.close()
                        println("Leltarhely beolvasva")

                        val editor: SharedPreferences.Editor =
                            sharedPreferencesIker.edit()
                        editor.putString(
                            "raktar",
                            "0"
                        )
                        editor.apply()
                        runOnUiThread {
                            Toast.makeText(this@MainActivity,
                                "leltarhely.txt beolvasva", Toast.LENGTH_SHORT).show() }
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
                connectFtp()
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                findViewById<ProgressBar>(R.id.progressBar).isVisible = true
            }
            .show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(getString(R.string.DeleteConfirmation))
            .setCancelable(false)
            .setNegativeButton(R.string.no) { _, _ -> }
            .setPositiveButton(R.string.yes) { _, _ ->
                GlobalScope.launch(Dispatchers.IO)
                {deleteCurrentListData(ItemRoomDatabase.getDatabase(this@MainActivity))}
            }
            .show()
    }

    private fun downloadFromCloud(){
        val ref = storageRef.child("app-debug.apk")
        ref.downloadUrl.addOnSuccessListener {
            downloadFiles(this, "app-debug", ".apk",
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(), "gs://hcinventory-e834e.appspot.com")
        }.addOnFailureListener{

        }

    }

    fun downloadFiles(
        context: Context,
        fileName: String,
        fileExtension: String,
        destinationDirectory: String?,
        url: String?
    ) {
        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(
            context,
            destinationDirectory,
            fileName + fileExtension
        )
        downloadManager.enqueue(request)
    }

    /*override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // If there's a download in progress, save the reference so you can query it later
        outState.putString("reference", storageRef.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // If there was a download in progress, get its reference and create a new StorageReference
        val stringRef = savedInstanceState.getString("reference") ?: return

        val storageRef = Firebase.storage.getReferenceFromUrl(stringRef)

        // Find all DownloadTasks under this StorageReference (in this example, there should be one)
        val tasks = storageRef.activeDownloadTasks

        if (tasks.size > 0) {
            // Get the task monitoring the download
            val task = tasks[0]

            // Add new listeners to the task using an Activity scope
            task.addOnSuccessListener(this) {
                // Success!
                // ...
            }
        }
    }*/


    private fun uploadToCloud(){
        val storage = Firebase.storage
        val storageRef = storage.reference

// Create a reference to "mountains.jpg"
        val mountainsRef = storageRef.child("mountains.jpg")

// Create a reference to 'images/mountains.jpg'
        val mountainImagesRef = storageRef.child("images/mountains.jpg")

// While the file names are the same, the references point to different files
        mountainsRef.name == mountainImagesRef.name // true
        mountainsRef.path == mountainImagesRef.path // false
        var file = Uri.fromFile(File("path/to/images/rivers.jpg"))
        val riversRef = storageRef.child("images/${file.lastPathSegment}")
        val uploadTask = riversRef.putFile(file)

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }

    /**
     * Handle navigation when the user chooses Up from the action bar.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}