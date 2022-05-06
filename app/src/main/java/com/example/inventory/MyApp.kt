package com.example.inventory

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // this method fires once as well as constructor
        // but also application has context here
        Log.i("main", "onCreate fired")
    }

    init {
        // this method fires only once per application start.
        // getApplicationContext returns null here
        Log.i("main", "Constructor fired")


        }
}