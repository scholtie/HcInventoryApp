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
import com.example.inventory.data.AllProductsDao
import com.example.inventory.data.Item
import kotlinx.coroutines.launch
import java.io.Console

/**
 * View Model to keep a reference to the Inventory repository and an up-to-date list of all items.
 *
 */
class AllProductsViewModel(private val allProductsDao: AllProductsDao) : ViewModel() {

    // Cache all items form the database using LiveData.
    val allItems: LiveData<List<AllProducts>> = allProductsDao.getItems().asLiveData()

    /**
     * Returns true if stock is available to sell, false otherwise.
     */
    /**
     * Updates an existing Item in the database.
     */
    fun updateAllProducts(
        productId: Int,
        productName: String,
        productBarcode: String,
        productPrice: String,
        productCount: String
    ) {
        val updatedItem = getUpdatedItemEntry(productId, productName, productBarcode, productPrice, productCount)
        updateItem(updatedItem)
    }


    /**
     * Launching a new coroutine to update an item in a non-blocking way
     */
    private fun updateItem(allProducts: AllProducts) {
        viewModelScope.launch {
            allProductsDao.update(allProducts)
        }
    }

    /**
     * Inserts the new Item into database.
     */
    fun addNewItem(productName: String, productBarcode: String, productPrice: String, productCount: String) {
        val newItem = getNewItemEntry(productName, productBarcode, productPrice, productCount)
        insertItem(newItem)
    }

    /**
     * Launching a new coroutine to insert an item in a non-blocking way
     */
    private fun insertItem(allProducts: AllProducts) {
        viewModelScope.launch {
            allProductsDao.insert(allProducts)
        }
    }

    fun deleteAll()
    {

    }
    /**
     * Launching a new coroutine to delete an item in a non-blocking way
     */
    fun deleteItem(allProducts: AllProducts) {
        viewModelScope.launch {
            allProductsDao.delete(allProducts)
        }
    }

    /**
     * Retrieve an item from the repository.
     */
    fun retrieveItem(id: Int): LiveData<AllProducts> {
        return allProductsDao.getItem(id).asLiveData()
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    fun isEntryValid(productName: String, productBarcode: String, productPrice: String, productCount: String): Boolean {
        if (productName.isBlank() || productBarcode.isBlank() || productPrice.isBlank() || productCount.isBlank()) {
            return false
        }
        return true
    }

    /**
     * Returns an instance of the [Item] entity class with the item info entered by the user.
     * This will be used to add a new entry to the Inventory database.
     */
    private fun getNewItemEntry(productName: String, productBarcode: String, productPrice: String, productCount: String): AllProducts {
        return AllProducts(
            productName = productName,
            productBarcode = productBarcode,
            productPrice = productPrice.toDouble(),
            productQuantityInStock = productCount.toInt()
        )
    }

    /**
     * Called to update an existing entry in the Inventory database.
     * Returns an instance of the [Item] entity class with the item info updated by the user.
     */
    private fun getUpdatedItemEntry(
        productId: Int,
        productName: String,
        productBarcode: String,
        productPrice: String,
        productCount: String
    ): AllProducts {
        return AllProducts(
            id = productId,
            productName = productName,
            productBarcode = productBarcode,
            productPrice = productPrice.toDouble(),
            productQuantityInStock = productCount.toInt()
        )
    }
}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class AllProductsViewModelFactory(private val allProductsDao: AllProductsDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllProductsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AllProductsViewModel(allProductsDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

