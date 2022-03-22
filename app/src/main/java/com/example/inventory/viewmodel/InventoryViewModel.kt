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

package com.example.inventory

import androidx.lifecycle.*
import com.example.inventory.data.AllProducts
import com.example.inventory.data.Item
import com.example.inventory.data.ItemDao
import kotlinx.coroutines.launch
import java.util.*

/**
 * View Model to keep a reference to the Inventory repository and an up-to-date list of all items.
 *
 */
class InventoryViewModel(private val itemDao: ItemDao) : ViewModel() {

    // Cache all items form the database using LiveData.
    val allItems: LiveData<List<Item>> = itemDao.getItems()
    //val itemsList: LiveData<List<Item>>

    /**
     * Returns true if stock is available to sell, false otherwise.
     */
    fun isStockAvailable(item: Item): Boolean {
        return (item.itemMennyiseg > 0)
    }

    /*init {
        itemsList = itemDao.allItems
    }*/

    /**
     * Updates an existing Item in the database.
     */
    fun updateItem(
        itemId: Int,
        itemAruid: Int,
        itemArunev: String,
        itemMennyiseg: Int,
        itemDatum: Double,
        itemTarolohelyid: String,
        itemUserid: Int,
        itemIker: Boolean
    ) {
        val updatedItem = getUpdatedItemEntry(itemId, itemAruid, itemArunev , itemMennyiseg, itemDatum, itemTarolohelyid, itemUserid, itemIker)
        updateItem(updatedItem)
    }


    /**
     * Launching a new coroutine to update an item in a non-blocking way
     */
    private fun updateItem(item: Item) {
        viewModelScope.launch {
            itemDao.update(item)
        }
    }

    fun retrieveMatchingAruid(vonalkod: Int): LiveData<Item>{
        return itemDao.searchBarcodeByAruid(vonalkod).asLiveData()
    }

    /**
     * Decreases the stock by one unit and updates the database.
     */
    fun sellItem(item: Item) {
        if (item.itemMennyiseg > 0) {
            // Decrease the quantity by 1
            val newItem = item.copy(itemMennyiseg = item.itemMennyiseg - 1)
            updateItem(newItem)
        }
    }

    /**
     * Inserts the new Item into database.
     */
    fun addNewItem(itemAruid: Int,
                   itemArunev: String,
                   itemMennyiseg: Int,
                   itemDatum: Double,
                   itemTarolohelyid: String,
                   itemUserid: Int,
                   itemIker: Boolean) {
        val newItem = getNewItemEntry(itemAruid, itemArunev, itemMennyiseg, itemDatum, itemTarolohelyid, itemUserid, itemIker)
        insertItem(newItem)
    }

    /**
     * Launching a new coroutine to insert an item in a non-blocking way
     */
    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }

    fun deleteAll()
    {

    }
    /**
     * Launching a new coroutine to delete an item in a non-blocking way
     */
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }

    /**
     * Retrieve an item from the repository.
     */
    fun retrieveItem(id: Int): LiveData<Item> {
        return itemDao.getItem(id).asLiveData()
    }



    /**
     * Returns true if the EditTexts are not empty
     */
    fun isEntryValid(
                     itemMennyiseg: String ): Boolean {
        if (itemMennyiseg.toString().isBlank()) {
            return false
        }
        return true
    }

    /**
     * Returns an instance of the [Item] entity class with the item info entered by the user.
     * This will be used to add a new entry to the Inventory database.
     */
    private fun getNewItemEntry(itemAruid: Int,
                                itemArunev: String,
                                itemMennyiseg: Int,
                                itemDatum: Double,
                                itemTarolohelyid: String,
                                itemUserid: Int,
                                itemIker: Boolean): Item {
        return Item(
            itemAruid = itemAruid,
            itemArunev = itemArunev,
            itemMennyiseg = itemMennyiseg,
            itemDatum = itemDatum,
            itemTarolohelyid = itemTarolohelyid,
            itemUserid = itemUserid,
            itemIker = itemIker
        )
    }

    /**
     * Called to update an existing entry in the Inventory database.
     * Returns an instance of the [Item] entity class with the item info updated by the user.
     */
    private fun getUpdatedItemEntry(
        itemId: Int,
        itemAruid: Int,
        itemArunev: String,
        itemMennyiseg: Int,
        itemDatum: Double,
        itemTarolohelyid: String,
        itemUserid: Int,
        itemIker: Boolean
    ): Item {
        return Item(
            id = itemId,
            itemAruid = itemAruid,
            itemArunev = itemArunev,
            itemMennyiseg = itemMennyiseg,
            itemDatum = itemDatum,
            itemTarolohelyid = itemTarolohelyid,
            itemUserid = itemUserid,
            itemIker = itemIker
        )
    }
}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class InventoryViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

