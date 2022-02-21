package com.example.inventory.datawedge

import java.io.Serializable

class Scan(val data: String, val symbology: String, val dateTime: String) :
    Serializable