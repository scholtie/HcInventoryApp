
package com.example.inventory

import android.app.Activity
import android.app.PendingIntent.CanceledException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.example.inventory.data.*
import com.example.inventory.databinding.ActivityAddNewItemBinding
import com.example.inventory.databinding.ActivityAddNewItemBinding.inflate
import com.example.inventory.service.DWUtilities
import com.example.inventory.viewmodel.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class AddNewItemActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var binding: ActivityAddNewItemBinding
    private val navigationArgs: AddNewItemActivityArgs by navArgs()
    private val viewModel: InventoryViewModel by viewModels {
        InventoryViewModelFactory(
            (this.application as InventoryApplication).database
                .itemDao()
        )
    }
    private val allProductsViewModel : AllProductsViewModel by viewModels {
        AllProductsViewModelFactory(
            (this.application as InventoryApplication).database.
            allProductsDao())
    }
    private val vonalkodViewModel : VonalkodViewModel by viewModels {
        VonalkodViewModelFactory(
            (this.application as InventoryApplication).database.
            vonalkodDao())
    }

    lateinit var item: Item
    lateinit var barcode: AllProducts
    private lateinit var barcodeVonalkod: Vonalkod

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DWUtilities.CreateDWProfile(this)
        binding = inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val sharedPreferencesIker = this.getSharedPreferences("IkerRaktar",
            Context.MODE_PRIVATE)
        val iker: String? = sharedPreferencesIker.getString("iker", "false")
        binding.checkBox.isChecked = iker.toBoolean()
        val id = navigationArgs.itemId
        if (id > 0) {
            viewModel.retrieveItem(id).observe(this) { selectedItem ->
                item = selectedItem
                bind(item)
            }
        } else {
            findViewById<Button>(R.id.save_action).setOnClickListener {
                addNewItem()
            }
        }
        lifecycleScope.launch {
            val leltarhelyList: List<Leltarhely> =
                ItemRoomDatabase.getDatabase(this@AddNewItemActivity).leltarhelyDao().getLeltarhelyek()
            if (leltarhelyList.isNotEmpty()) {
                loadSpinnerData()
                binding.spnLeltarhely.isEnabled = true
            } else {
                binding.spnLeltarhely.isEnabled = false
            }
        }
        if (intent.hasExtra(resources.getString(R.string.datawedge_intent_key_data))){onNewIntent(intent)}
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        displayScanResult(intent)
        showItemWithBarcode()
        binding.itemCount.requestFocus()
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun displayScanResult(scanIntent: Intent) {
        val decodedSource =
            scanIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_source))
        val decodedData =
            scanIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_data))
        val decodedLabelType =
            scanIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_label_type))
        val scan = "$decodedData"
        val output = binding.itemBarcode
        output.setText(scan)
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
            if (motionEvent?.action == MotionEvent.ACTION_DOWN) {
                //  Button pressed, start scan
                val dwIntent = Intent()
                dwIntent.action = "com.symbol.datawedge.api.ACTION"
                dwIntent.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER",
                    "START_SCANNING")
                view!!.performClick()
                sendBroadcast(dwIntent)
            } else if (motionEvent?.action == MotionEvent.ACTION_UP) {
                //  Button released, end scan
                val dwIntent = Intent()
                dwIntent.action = "com.symbol.datawedge.api.ACTION"
                dwIntent.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER",
                    "STOP_SCANNING")
                sendBroadcast(dwIntent)
            }
        return true
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.itemCount.text.toString(),
            binding.itemBarcode.text.toString()
        )
    }

    /**
     * Binds views with the passed in [item] information.
     */
    private fun bind(item: Item) {
        //val price = "%.2f".format(item.itemPrice)
        binding.apply {
            itemCount.setText(item.itemMennyiseg.toString(), TextView.BufferType.SPANNABLE)
            txtName.setText(item.itemArunev,TextView.BufferType.SPANNABLE )
            txtAruid.setText(item.itemAruid.toString(),TextView.BufferType.SPANNABLE)
            checkBox.isChecked = item.itemIker
            spnLeltarhely.setSelection(item.itemTarolohelyid)
            saveAction.setOnClickListener { updateItem() }
            itemCount.isFocusableInTouchMode = true
            itemCount.requestFocus()
            itemCount.setSelection(itemCount.length())
            itemBarcode.isVisible = false
            itemBarcodeLabel.isVisible = false
        }
    }

    private fun bindAruid(aruid: AllProducts) {
        binding.apply { txtName.setText(aruid.productCikknev, TextView.BufferType.SPANNABLE)
            txtPrice.setText(aruid.productBrfogyar.toString() + " Ft",
                TextView.BufferType.SPANNABLE)
            txtAruid.setText(aruid.productId.toString(), TextView.BufferType.SPANNABLE)}
    }

    private fun loadSpinnerData() {
        val sharedPreferencesIker = this.getSharedPreferences("IkerRaktar",
            Context.MODE_PRIVATE)
        val raktar: String? = sharedPreferencesIker.getString("raktar", "0")
        lifecycleScope.launch {
            val spinner: Spinner = binding.spnLeltarhely
            // database handler
            val db = ItemRoomDatabase.getDatabase(this@AddNewItemActivity)

            // Spinner Drop down elements
            val labels: List<String> = db.leltarhelyDao().getAllLeltarhely()

            // Creating adapter for spinner
            val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                this@AddNewItemActivity,
                android.R.layout.simple_spinner_item, labels
            )

            // Drop down layout style - list view with radio button
            dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // attaching data adapter to spinner
            spinner.adapter = dataAdapter
            binding.spnLeltarhely.setSelection(raktar!!.toInt())
        }
    }

    private fun dateDiff(): String {
        /*val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z", Locale.ENGLISH)
        val firstDate = sdf.parse("1899.12.30 AD at 00:00:00 PDT")
        val secondDate = Date()
        val diffInMillies = abs(secondDate.time - firstDate!!.time)
        val diff: Long = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)
        val diffDouble: Double = diff.toDouble()
        //assertEquals(6, diff)
        println(diffDouble)
        return diffDouble*/
        val sdf = SimpleDateFormat("yyyy.M.dd.hh.mm.ss")
        return sdf.format(Date()).toString()
    }

    private fun getIkerleltar(): Boolean{
        return binding.checkBox.isChecked
    }

    /**
     * Inserts the new Item into database and navigates up to list fragment.
     */
    private fun addNewItem() {
        //dateDiff()
        val sharedPreferences = this.getSharedPreferences("Users", Context.MODE_PRIVATE)
        val sharedPreferencesIker = this.getSharedPreferences(
            "IkerRaktar", Context.MODE_PRIVATE)
        val userId: String? = sharedPreferences.getString("id", "0")
        //val dateTime = Calendar.getInstance().time.time
        //val dateTimeAsDouble = dateTime.toDouble()
        if (isEntryValid()) {
            if (binding.itemCount.text.toString().toDouble() != 0.0) {
                if (userId != ""){
            val barcodeValue = binding.itemBarcode.text.toString()
            vonalkodViewModel.retrieveMatchingAruid(barcodeValue)
                .observe(this) { barcodeTest ->
                    try {
                        barcodeVonalkod = barcodeTest
                        allProductsViewModel.retrieveMatchingBarcode(barcodeVonalkod.vonalkodAruid)
                            .observe(this) { aruid ->
                                try {
                                    barcode = aruid
                                    viewModel.addNewItem(
                                        barcode.productId,
                                        aruid.productCikkszam,
                                        aruid.productCikknev,
                                        binding.itemCount.text.toString().toDouble(),
                                        dateDiff(),
                                        binding.spnLeltarhely.selectedItemPosition,
                                        userId!!.toInt(),
                                        getIkerleltar()
                                    )
                                    val switchActivityIntent = Intent(
                                        this,
                                        MainActivity::class.java
                                    )
                                    try {
                                        hideKeyboard()
                                        startActivity(switchActivityIntent)
                                        val editor: SharedPreferences.Editor =
                                            sharedPreferencesIker.edit()
                                        editor.putString("iker", getIkerleltar().toString())
                                        editor.putString(
                                            "raktar",
                                            binding.spnLeltarhely.selectedItemPosition.toString()
                                        )
                                        editor.apply()
                                    } catch (e: CanceledException) {
                                        e.printStackTrace()
                                    }
                                } catch (e: Exception) {
                                    println(getString(R.string.itemnotfound))
                                }
                            }
                    } catch (e: Exception) {
                        showNewItemConfirmationDialog()
                        binding.txtName.text = ""
                        binding.txtAruid.text = ""
                        binding.txtPrice.text = ""
                    }

                }}
            else{Toast.makeText(this, getString(R.string.nouser), Toast.LENGTH_SHORT).show()
                    val switchActivityIntent = Intent(
                        this,
                        LoginActivity::class.java)
                    try {
                        hideKeyboard()
                        startActivity(switchActivityIntent)
                        val editor: SharedPreferences.Editor =
                            sharedPreferencesIker.edit()
                        editor.putString("iker", getIkerleltar().toString())
                        editor.putString(
                            "raktar",
                            binding.spnLeltarhely.selectedItemPosition.toString()
                        )
                        editor.apply()
                    } catch (e: CanceledException) {
                        e.printStackTrace()
                    }}
        }else{
                Toast.makeText(this, getString(R.string.nemnulla), Toast.LENGTH_SHORT).show()
            }

        }
        else{
            Toast.makeText(this, getString(R.string.osszesmezo), Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Updates an existing Item in the database and navigates up to list fragment.
     */
    private fun updateItem() {
        val sharedPreferences = this.getSharedPreferences("Users", Context.MODE_PRIVATE)
        val userId: String? = sharedPreferences.getString("id", "0")
        if (binding.itemCount.text.toString().isNotEmpty() &&
            binding.itemCount.text.toString().toDouble() != 0.0) {
            viewModel.updateItem(
                this.navigationArgs.itemId,
                item.itemAruid,
                item.itemCikkszam,
                item.itemArunev,
                binding.itemCount.text.toString().toDouble(),
                dateDiff(),
                binding.spnLeltarhely.selectedItemPosition,
                userId!!.toInt(),
                getIkerleltar()
            )
            hideKeyboard()
            val switchActivityIntent = Intent(this, MainActivity::class.java)
            try {
                startActivity(switchActivityIntent)
            } catch (e: CanceledException) {
                e.printStackTrace()
            }
        }
        else{
            Toast.makeText(this, getString(R.string.osszesmezo), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showItemWithBarcode() {
        val barcodeValue = binding.itemBarcode.text.toString()
        if (barcodeValue.isNotEmpty())
            vonalkodViewModel.retrieveMatchingAruid(barcodeValue)
                .observe(this) { barcodeTest ->
                    try {
                        barcodeVonalkod = barcodeTest
                        allProductsViewModel.retrieveMatchingBarcode(barcodeVonalkod.vonalkodAruid)
                            .observe(this) { aruid ->
                                try {
                                    barcode = aruid
                                    bindAruid(barcode)
                                }
                                catch (e: Exception){
                                    println(getString(R.string.itemnotfound))

                                }                                }
                    } catch (e: Exception){
                        showNewItemConfirmationDialog()
                    }

                }
        else{
            Toast.makeText(this, getString(R.string.scanbarcode), Toast.LENGTH_SHORT).show()
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
        binding.itemBarcode.setText("")
        //val productBarcode = binding.itemBarcode.text.toString()
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage(getString(R.string.barcodenotfound))
            .setCancelable(false)
            .setNeutralButton("Ok"){ _, _ -> }
            /*.setNegativeButton("Nem") { _, _ -> }
            .setPositiveButton("Igen") { _, _ -> val action =
                AddItemFragmentDirections.actionAddItemFragmentToAddAllProductsFragment(0, productBarcode)
                findNavController().navigate(action)
            }*/
            .show()
    }

    /*override fun onBackPressed() {
        super.onBackPressed()
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        try {
            startActivity(switchActivityIntent)
        } catch (e: CanceledException) {
            e.printStackTrace()
        }
    }*/

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE)
                as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    }

