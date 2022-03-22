package com.example.inventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Users(
    @PrimaryKey
    val userId: Int,
    @ColumnInfo(name = "name")
    val userName: String,
    @ColumnInfo(name = "password")
    val userPass: String
)