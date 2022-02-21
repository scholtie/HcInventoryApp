package com.example.inventory.extension

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.inventory.datawedge.PROFILE_INTENT_ACTION
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun Context.showConfirmation(message: String) = suspendCoroutine<Boolean> {
    val dialog = AlertDialog.Builder(this)
        .setTitle("Figyelem!")
        .setMessage(message)
        .setPositiveButton("Ok") { _, _ -> it.resume(true) }
        .setNegativeButton("Mégsem") { _, _ -> it.resume(false) }
        .setCancelable(false)
        .create()
    dialog.show()
}

suspend fun Context.showError(message: String) = suspendCoroutine<Boolean> {
    val dialog = AlertDialog.Builder(this)
        .setTitle("Hiba!")
        .setMessage(message)
        .setPositiveButton("Ok") { _, _ -> it.resume(true) }
        .setCancelable(false)
        .create()
    dialog.show()
}

suspend fun Context.showInformation(message: String) = suspendCoroutine<Boolean> {
    val dialog = AlertDialog.Builder(this)
        .setTitle("Információ")
        .setMessage(message)
        .setPositiveButton("Ok") { _, _ -> it.resume(true) }
        .setCancelable(false)
        .create()
    dialog.show()
}

fun Context.showToast(message: String) {
    val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    toast.show()
}

fun Context.registerBarcodeReceiver(receiver: BroadcastReceiver) {
    val intentFilter = IntentFilter()
    intentFilter.addAction(PROFILE_INTENT_ACTION)
    intentFilter.addCategory("android.intent.category.DEFAULT")
    this.registerReceiver(receiver, intentFilter)
}
