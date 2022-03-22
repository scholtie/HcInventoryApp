/*
package com.example.inventory.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.inventory.datawedge.DataWedgeInterface

abstract class BarcodeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.hasExtra(DataWedgeInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)) {
            //  Handle scan intent received from DataWedge, add it to the list of scans
            val scanData =
                intent.getStringExtra(DataWedgeInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)
            val symbology =
                intent.getStringExtra(DataWedgeInterface.DATAWEDGE_SCAN_EXTRA_LABEL_TYPE)
            if (scanData != null)
                onBarcodeReceived(scanData, symbology)
        }
    }

    abstract fun onBarcodeReceived(barcode: String, format: String?)
}*/
