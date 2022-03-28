package com.example.inventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Leltarhely(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val leltarhelyId: Int,
    @ColumnInfo(name = "name")
    val leltarhelyName: String,
)