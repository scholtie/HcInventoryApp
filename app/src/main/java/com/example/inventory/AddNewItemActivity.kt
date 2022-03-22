package com.example.inventory

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgs
import com.example.inventory.data.AllProducts
import com.example.inventory.data.Item
import com.example.inventory.data.Vonalkod
import com.example.inventory.databinding.FragmentAddItemBinding
import com.example.inventory.service.DWUtilities
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddNewItemActivity : AppCompatActivity(), View.OnTouchListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_item)
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
        if (view?.getId() == R.id.btnScan) {
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
}