/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.inventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.*

/**
 * Entity data class represents a single row in the database.
 */
@Entity
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "aruid")
    val itemAruid: Int,
    @ColumnInfo(name = "arunev")
    val itemArunev: String,
    @ColumnInfo(name = "tarolohelyid")
    val itemTarolohelyid: String,
    @ColumnInfo(name = "mennyiseg")
    val itemMennyiseg: Int,
    @ColumnInfo(name = "userid")
    val itemUserid: Int,
    @ColumnInfo(name = "datum")
    val itemDatum: Double,
    @ColumnInfo(name = "iker")
    val itemIker: Boolean)
{
    companion object {
        const val TABLE_NAME = "items"
        const val TITLE = "title"
        const val DIRECTOR_ID = "directorId"
    }
}


/**
 * Returns the passed in price in currency format.
 */
/*fun Item.getFormattedPrice(): String =
    NumberFormat.getCurrencyInstance().format(itemPrice)*/

