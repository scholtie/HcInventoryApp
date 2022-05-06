package com.example.inventory

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.inventory.extension.registerBarcodeReceiver
import com.example.inventory.extension.toStringOrEmpty
import com.example.inventory.service.BarcodeReceiver
import com.example.inventory.service.DWUtilities
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.zxing.integration.android.IntentIntegrator

class TestActivity : AppCompatActivity(), View.OnTouchListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        DWUtilities.CreateDWProfile(this)
        val btnScan = findViewById<Button>(R.id.btnScan)
        btnScan.setOnTouchListener(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        displayScanResult(intent)
    }

    private fun displayScanResult(scanIntent: Intent) {
        val decodedSource =
            scanIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_source))
        val decodedData =
            scanIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_data))
        val decodedLabelType =
            scanIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_label_type))
        val scan = "$decodedData [$decodedLabelType]\n\n"
        val output = findViewById<TextView>(R.id.txtOutput)
        output.text = scan + output.text
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        if (view?.id == R.id.btnScan) {
            if (motionEvent?.action == MotionEvent.ACTION_DOWN) {
                //  Button pressed, start scan
                val dwIntent = Intent()
                dwIntent.action = "com.symbol.datawedge.api.ACTION"
                dwIntent.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "START_SCANNING")
                sendBroadcast(dwIntent)
            } else if (motionEvent?.action == MotionEvent.ACTION_UP) {
                //  Button released, end scan
                val dwIntent = Intent()
                dwIntent.action = "com.symbol.datawedge.api.ACTION"
                dwIntent.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "STOP_SCANNING")
                sendBroadcast(dwIntent)
            }
        }
        return true
    }
}