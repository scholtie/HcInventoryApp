package com.example.inventory

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.example.inventory.extension.registerBarcodeReceiver
import com.example.inventory.extension.toStringOrEmpty
import com.example.inventory.service.BarcodeReceiver
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.zxing.integration.android.IntentIntegrator

class BarcodeTest : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_test)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        wireUpUI()
    }

    override fun onResume() {
        super.onResume()
        startBarcodeReceiver()
    }

    override fun onPause() {
        super.onPause()
        stopBarcodeReceiver()
    }

    private fun wireUpUI() {
        findViewById<TextView>(R.id.valueBarcodeLength).text = ""
        findViewById<TextView>(R.id.valueBarcodeType).text = ""
        findViewById<TextView>(R.id.valueBarcode).text = ""

        loadPreferences()
    }

    private fun scanWithCamera() {
        run {
            val integrator = IntentIntegrator(this@BarcodeTest)
            integrator.setPrompt("Vonalkód beolvasás")
            integrator.setOrientationLocked(true)
            val formats = listOf(
                IntentIntegrator.CODE_128,
                IntentIntegrator.CODE_39,
                IntentIntegrator.CODE_93,
                IntentIntegrator.EAN_13,
                IntentIntegrator.EAN_8
            )
            integrator.setDesiredBarcodeFormats(formats)
            integrator.initiateScan()
        }
    }

    private val barcodeReceiver: BarcodeReceiver = object : BarcodeReceiver() {
        override fun onBarcodeReceived(barcode: String, format: String?) {
            barcodeReceived(barcode, format.toStringOrEmpty())
        }
    }

    private fun barcodeReceived(barcode: String, format: String) {
        if (barcode != "") {
            findViewById<TextView>(R.id.valueBarcodeLength).text = barcode.length.toString()
            findViewById<TextView>(R.id.valueBarcodeType).text = format.toStringOrEmpty()
            findViewById<TextView>(R.id.valueBarcode).text = barcode
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun stopBarcodeReceiver() {
        unregisterReceiver(barcodeReceiver)
    }

    private fun startBarcodeReceiver() {
        registerBarcodeReceiver(barcodeReceiver)
    }

    private fun loadPreferences() {
        val useCamera = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val scanningResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanningResult != null) {
            if (resultCode == Activity.RESULT_OK) {
                barcodeReceived(scanningResult.contents, scanningResult.formatName)
            }
        }
    }
}