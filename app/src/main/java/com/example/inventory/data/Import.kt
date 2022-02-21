package com.example.inventory.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "import")
data class Import(
    @PrimaryKey
    val sourceFileName: String,
    val importedAt: Date,
    var lastExport: Int
)