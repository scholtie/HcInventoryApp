
package com.example.inventory

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgs
import com.example.inventory.data.AllProducts
import com.example.inventory.data.Item
import com.example.inventory.data.Vonalkod
import com.example.inventory.databinding.ActivityAddNewItemBinding
import com.example.inventory.databinding.ActivityAddNewItemBinding.inflate
import com.example.inventory.databinding.FragmentAddItemBinding
import com.example.inventory.service.DWUtilities
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddNewItemActivity : AppCompatActivity(), View.OnTouchListener {


    private lateinit var navController: NavController
    private lateinit var binding: ActivityAddNewItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DWUtilities.CreateDWProfile(this)
        binding = inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val btnScan = findViewById<Button>(R.id.btnScan)
        //btnScan.setOnTouchListener(this)
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
            /*findViewById<Button>(R.id.showItemWithBarcode_action).setOnClickListener {
                showItemWithBarcode()
            }*/
            //binding.btnExportCSV.setOnClickListener { exportDatabaseToCSVFile() }
        }
/*        _binding = AddNewItemActivity.inflate(inflater, container, false)
        return binding.root*/

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        displayScanResult(intent)
        showItemWithBarcode()
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
        if (view?.getId() == R.id.btnScanFloating) {
            if (motionEvent?.getAction() == MotionEvent.ACTION_DOWN) {
                //  Button pressed, start scan
                val dwIntent = Intent()
                dwIntent.action = "com.symbol.datawedge.api.ACTION"
                dwIntent.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "START_SCANNING")
                sendBroadcast(dwIntent)
            } else if (motionEvent?.getAction() == MotionEvent.ACTION_UP) {
                //  Button released, end scan
                val dwIntent = Intent()
                dwIntent.action = "com.symbol.datawedge.api.ACTION"
                dwIntent.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "STOP_SCANNING")
                sendBroadcast(dwIntent)
            }
        }
        return true
    }

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
    private val navigationArgs: ItemDetailFragmentArgs by navArgs()

    lateinit var item: Item
    lateinit var barcode: AllProducts
    lateinit var barcodeVonalkod: Vonalkod
    private var quantity: Int = 0
    private lateinit var itemListAdapter: ItemListAdapter


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
            //itemName.setText(item.itemAruid, TextView.BufferType.SPANNABLE)
            //itemBarcode.setText(item.itemTarolohelyid, TextView.BufferType.SPANNABLE)
            //itemPrice.setText(item.itemDatum.toString(), TextView.BufferType.SPANNABLE)
            itemCount.setText(item.itemMennyiseg.toString(), TextView.BufferType.SPANNABLE)
            saveAction.setOnClickListener { updateItem() }
            /*btnIncreaseQuantity.setOnClickListener { increaseQuantity() }
            btnDecreaseQuantity.setOnClickListener { decreaseQuantity() }*/
            //showItemWithBarcodeAction.setOnClickListener{ showItemWithBarcode() }
            /*itemBarcode.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    showItemWithBarcode()
                }
            }*/
            itemBarcode.isEnabled = false
        }
    }

    private fun bindBarcode(barcodeVonalkod: Vonalkod) {
        binding.apply { txtName.setText(barcodeVonalkod.vonalkodAruid.toString(), TextView.BufferType.SPANNABLE)
        }
    }

    private fun bindAruid(aruid: AllProducts) {
        binding.apply { txtName.setText(aruid.productCikknev, TextView.BufferType.SPANNABLE)
            txtPrice.setText(aruid.productBrfogyar.toString() + " Ft", TextView.BufferType.SPANNABLE)
            txtAruid.setText(aruid.productId.toString(), TextView.BufferType.SPANNABLE)}
    }

    /**
     * Inserts the new Item into database and navigates up to list fragment.
     */
    private fun addNewItem() {
        val sharedPreferences = this.getSharedPreferences("Users", Context.MODE_PRIVATE)
        val userId: String? = sharedPreferences.getString("id", "0")
        if (isEntryValid()) {
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
                                        aruid.productCikknev,
                                        binding.itemCount.text.toString().toInt(),
                                        10.0,
                                        "testTarolohely",
                                        userId!!.toInt(),
                                        false
                                    )
                                    val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
                                    navController.navigateUp()
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
            Toast.makeText(this, "Kérem töltse ki az összes mezőt!", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Updates an existing Item in the database and navigates up to list fragment.
     */
    private fun updateItem() {
        if (isEntryValid()) {
            viewModel.updateItem(
                this.navigationArgs.itemId,
                item.itemAruid,
                item.itemArunev,
                binding.itemCount.text.toString().toInt(),
                item.itemDatum,
                item.itemTarolohelyid,
                item.itemUserid,
                item.itemIker
            )
            val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
            navController.navigate(action)
        }
        else{
            Toast.makeText(this, "Kérem töltse ki az összes mezőt!", Toast.LENGTH_SHORT).show()
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
                                    println("Nem talált termék")

                                }                                }
                    } catch (e: Exception){
                        showNewItemConfirmationDialog()
                    }

                }
        else{
            Toast.makeText(this, "Kérem olvassa be a vonalkódot", Toast.LENGTH_SHORT).show()
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
        MaterialAlertDialogBuilder(this)
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


    }

    /**
     * Called before fragment is destroyed.
     */

