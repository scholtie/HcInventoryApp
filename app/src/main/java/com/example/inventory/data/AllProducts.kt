package com.example.inventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["cikkszam"], unique = true)])
data class AllProducts(
    @PrimaryKey
    val productId: Int,
    @ColumnInfo(name = "cikkszam")
    val productCikkszam: String,
    @ColumnInfo(name = "cikknev")
    val productCikknev: String,
    @ColumnInfo(name = "beszerzesiar")
    val productUtbeszar: Int,
    @ColumnInfo(name = "bruttofogyasztoiar")
    val productBrfogyar: Int,
    @ColumnInfo(name = "egesz")
    val productEgesz: Int,
    @ColumnInfo(name = "PLU")
    val productPlu: Int,
    @ColumnInfo(name = "PLUszekcio")
    val productPluszekcio: Int,
    @ColumnInfo(name = "afaszazalek")
    val productAfaszaz: Int,
    @ColumnInfo(name = "bruttofogyar2")
    val productBrfogyar2: Int,
    @ColumnInfo(name = "bruttofogyar3")
    val productBrfogyar3: Int,
    @ColumnInfo(name = "bruttofogyar4")
    val productBrfogyar4: Int,
)