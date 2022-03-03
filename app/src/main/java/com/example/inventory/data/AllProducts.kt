package com.example.inventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AllProducts(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val productName: String,
    @ColumnInfo(name = "barcode")
    val productBarcode: String,
    @ColumnInfo(name = "price")
    val productPrice: Double,
    @ColumnInfo(name = "quantity")
    val productQuantityInStock: Int,
)