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

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Database access object to access the Inventory database
 */
@Dao
interface ItemDao {

    @get:Query("SELECT * FROM item ORDER BY name ASC")
    val allItems: LiveData<List<Item>>

    @Query("delete from item")
    suspend fun clear()

    @Query("select count(barcode) from item")
    suspend fun getCount(): Int

    @Query("SELECT * FROM item")
    fun getAll(): List<Item>

    @Query("SELECT * from item ORDER BY name ASC")
    fun getItems(): Flow<List<Item>>

    @Query("SELECT * from item WHERE id = :id")
    fun getItem(id: Int): Flow<Item>

    // Specify the conflict strategy as IGNORE, when the user tries to add an
    // existing Item into the database Room ignores the conflict.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item)

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("select * from item where quantity is not null")
    suspend fun findLeltarozott(): List<Item>
}
