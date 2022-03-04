package com.example.inventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["barcode"], unique = true)])
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