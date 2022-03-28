package com.example.inventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Vonalkod(
    @PrimaryKey
    val vonalkodAruid: Int,
    @ColumnInfo(name = "barcode")
    var vonalkodBarcode: String,
    @ColumnInfo(name = "karton")
    val vonalkodKarton: Int
    )