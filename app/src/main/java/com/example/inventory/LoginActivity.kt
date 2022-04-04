package com.example.inventory

import android.R
import android.content.*
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.example.inventory.data.*
import com.example.inventory.databinding.LoginActivityBinding
import com.example.inventory.service.DWUtilities
import com.example.inventory.service.MyFTPClientFunctions
import com.example.inventory.viewmodel.UsersViewModel
import com.example.inventory.viewmodel.UsersViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding
    private lateinit var navController: NavController
    private var ftpclient: MyFTPClientFunctions? = null
    private var isLoading: Boolean = false

    private val usersViewModel : UsersViewModel by viewModels(){
        UsersViewModelFactory(
            (this.application as InventoryApplication).database.
            usersDao())
    }
    lateinit var user: Users

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DWUtilities.CreateDWProfile(this)
        binding = LoginActivityBinding.inflate(layoutInflater)
        val view = binding.root
        ftpclient = MyFTPClientFunctions()
        binding.btnLoadData.setOnClickListener { showConfirmationDialog() }
        binding.btnLoadUsers.setOnClickListener { loadSpinnerData() }
        binding.loginBtn.setOnClickListener {loginAction() }
        setContentView(view)
        lifecycleScope.launch {
            val userList: List<Users> =
                ItemRoomDatabase.getDatabase(this@LoginActivity).usersDao().getUsers()
            if (userList.isNotEmpty()) {
                loadSpinnerData()
                binding.btnLoadData.isVisible = false
            } else {
                binding.loginBtn.isVisible = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.example.inventory.R.menu.loginftpsettings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.example.inventory.R.id.action_db_delete -> {
                val switchActivityIntent = Intent(this, FtpLoginActivity::class.java)
                startActivity(switchActivityIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadSpinnerData() {
        lifecycleScope.launch {
            val spinner: Spinner = binding.userSpinner
            // database handler
            val db = ItemRoomDatabase.getDatabase(this@LoginActivity)

            // Spinner Drop down elements
            val labels: List<String> = db.usersDao().getAllUserNames()

            // Creating adapter for spinner
            val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                this@LoginActivity,
                R.layout.simple_spinner_item, labels
            )

            // Drop down layout style - list view with radio button
            dataAdapter
                .setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

            // attaching data adapter to spinner
            spinner.adapter = dataAdapter
        }
        //binding.loginBtn.isEnabled = true
    }

    private fun loginAction(){
        val currentUser = binding.userSpinner.selectedItem.toString()
        usersViewModel.retrieveMatchingUser(currentUser)
            .observe(this) { userid ->
                try {
                    user = userid
                    val sharedPreferences = this
                        .getSharedPreferences("Users", Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString("user", currentUser)
                    editor.putString("id", user.userId.toString())
                    editor.apply()
                } catch (e: Exception) {
                    println("Nem talált termék")
                }
                if (binding.editPassword.text.toString() == user.userPass) {
                    val switchActivityIntent = Intent(this, MainActivity::class.java)
                    startActivity(switchActivityIntent)
                } else {
                    Toast.makeText(this, "Hibás jelszó!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private suspend fun fetchDocs() =
        coroutineScope {
            val deferredOne =
                async { populateDbAllProducts(ItemRoomDatabase.getDatabase(this@LoginActivity)) }
            val deferredTwo =
                async { populateDbUserek(ItemRoomDatabase.getDatabase(this@LoginActivity)) }
            val deferredThree =
                async { populateDbVonalkodok(ItemRoomDatabase.getDatabase(this@LoginActivity)) }
            val deferredFour =
                async {populateDbLeltarhely(ItemRoomDatabase.getDatabase(this@LoginActivity))}
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
                    runOnUiThread { Toast.makeText(this@LoginActivity, "cikk.txt fájl nem létezik", Toast.LENGTH_SHORT).show() }
                } finally {
                    try {
                        br?.close()
                        println("Termékek beolvasva")
                        runOnUiThread { Toast.makeText(this@LoginActivity, "cikk.txt beolvasva", Toast.LENGTH_SHORT).show() }
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
                        //findViewById<ProgressBar>(com.example.inventory.R.id.progressBar).isVisible = true
                        i++
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread { Toast.makeText(this@LoginActivity, "vonalkod.txt fájl nem létezik", Toast.LENGTH_SHORT).show() }
                } finally {
                    try {
                        brVk?.close()
                        println("Vonalkódok beolvasva")
                        runOnUiThread {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            //findViewById<ProgressBar>(com.example.inventory.R.id.progressBar).isVisible = false
                            runOnUiThread {
                                findViewById<ProgressBar>(com.example.inventory.R.id.progressBar4).isVisible = false }
                            isLoading = false
                            Toast.makeText(this@LoginActivity, "vonalkod.txt beolvasva", Toast.LENGTH_SHORT).show()
                            val switchActivityIntent = Intent(this@LoginActivity, LoginActivity::class.java)
                            startActivity(switchActivityIntent)
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
                    runOnUiThread { Toast.makeText(this@LoginActivity, "ugyintezo.txt fájl nem létezik", Toast.LENGTH_SHORT).show() }
                } finally {
                    try {
                        brUs?.close()
                        println("Userek beolvasva")
                        runOnUiThread { Toast.makeText(this@LoginActivity, "ugyintezo.txt beolvasva", Toast.LENGTH_SHORT).show() }
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
                    runOnUiThread { Toast.makeText(this@LoginActivity, "leltarhely.txt fájl nem létezik", Toast.LENGTH_SHORT).show() }
                } finally {
                    try {
                        brUs?.close()
                        println("Leltarhely beolvasva")
                        runOnUiThread { Toast.makeText(this@LoginActivity, "leltarhely.txt beolvasva", Toast.LENGTH_SHORT).show() }
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
            .setMessage("Adatbázis írása a fájlokból. Biztosan folytatni szeretné? A folyamat akár 10 percig is tarthat.")
            .setCancelable(false)
            .setNegativeButton("Nem") { _, _ -> }
            .setPositiveButton("Igen") { _, _ ->
                connectFtp()
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                binding.btnLoadData.isEnabled = false
                //findViewById<ProgressBar>(com.example.inventory.R.id.progressBar).isVisible = true
            }
            .show()
    }

    private fun connectFtp(){
        runOnUiThread {
            findViewById<ProgressBar>(com.example.inventory.R.id.progressBar4).isVisible = true }
        val sharedPreferencesFtp = this.getSharedPreferences("FtpDetails", Context.MODE_PRIVATE)
        val srcFilePath: String? = sharedPreferencesFtp.getString("path", "")
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
                Log.d(ContentValues.TAG, "Connection Success")
                runOnUiThread { Toast.makeText(this@LoginActivity, "Sikeres csatlakozás a szerverhez", Toast.LENGTH_SHORT).show() }
                ftpclient!!.ftpChangeDirectory(srcFilePath!!)
                downloadFtp()
                isLoading = true
                ftpclient!!.ftpPrintFilesList(srcFilePath)
            } else {
                runOnUiThread {
                    findViewById<ProgressBar>(com.example.inventory.R.id.progressBar4).isVisible = false }
                Log.d(ContentValues.TAG, "Connection failed")
                runOnUiThread { Toast.makeText(this@LoginActivity, "Letöltés Sikertelen", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.getItem(0).isEnabled = !isLoading
        return true
    }

    private fun downloadFtp(){
        val sharedPreferencesFtp = this.getSharedPreferences("FtpDetails", Context.MODE_PRIVATE)
        val srcFilePath: String? = sharedPreferencesFtp.getString("path", "")
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
        }
        catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                findViewById<ProgressBar>(com.example.inventory.R.id.progressBar3).isVisible = false }
        }
        finally {
            GlobalScope.launch(Dispatchers.IO) { fetchDocs() }
        }

    }
    override fun onBackPressed() {
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_dialog_alert)
            .setTitle("Kilépés")
            .setMessage("Biztosan ki szeretne lépni?")
            .setPositiveButton("Igen") { _, _ -> finishAffinity() }
            .setNegativeButton("Nem", null)
            .show()
    }
}