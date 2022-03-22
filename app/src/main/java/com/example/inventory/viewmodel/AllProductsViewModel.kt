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
        productAfaszaz: Int,
        productBrfogyar: Int,
        productBrfogyar2: Int,
        productBrfogyar3: Int,
        productBrfogyar4: Int,
        productCikknev: String,
        productCikkszam: String,
        productEgesz: Int,
        productPlu: Int,
        productPluszekcio: Int,
        productUtbeszar: Int
    ) {
        val updatedItem = getUpdatedItemEntry(
            productId,
            productAfaszaz,
            productBrfogyar,
            productBrfogyar2,
            productBrfogyar3,
            productBrfogyar4,
            productCikknev,
            productCikkszam,
            productEgesz,
            productPlu,
            productPluszekcio,
            productUtbeszar
        )
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

    fun retrieveMatchingBarcode(productid: Int): LiveData<AllProducts>{
        return allProductsDao.searchBarcode(productid).asLiveData()
    }

    /**
     * Inserts the new Item into database.
     */
    fun addNewItem(
        productId: Int,
        productAfaszaz: Int,
        productBrfogyar: Int,
        productBrfogyar2: Int,
        productBrfogyar3: Int,
        productBrfogyar4: Int,
        productCikknev: String,
        productCikkszam: String,
        productEgesz: Int,
        productPlu: Int,
        productPluszekcio: Int,
        productUtbeszar: Int
    ) {
        val newItem = getNewItemEntry(
            productId,
            productAfaszaz,
            productBrfogyar,
            productBrfogyar2,
            productBrfogyar3,
            productBrfogyar4,
            productCikknev,
            productCikkszam,
            productEgesz,
            productPlu,
            productPluszekcio,
            productUtbeszar
        )
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

    suspend fun deleteAll()
    {
        allProductsDao.clear()
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
    /*fun isEntryValid(
        productId: Int,
        productAfaszaz: Int,
        productBrfogyar: Int,
        productBrfogyar2: Int,
        productBrfogyar3: Int,
        productBrfogyar4: Int,
        productCikknev: String,
        productCikkszam: Int,
        productEgesz: Int,
        productPlu: Int,
        productPluszekcio: Int,
        productUtbeszar: Int
    ): Boolean {
        if (productCikknev.isBlank() || productBarcode.isBlank() || productPrice.isBlank() || productCount.isBlank()) {
            return false
        }
        return true
    }*/

    /**
     * Returns an instance of the [Item] entity class with the item info entered by the user.
     * This will be used to add a new entry to the Inventory database.
     */
    private fun getNewItemEntry(
        productId: Int,
        productAfaszaz: Int,
        productBrfogyar: Int,
        productBrfogyar2: Int,
        productBrfogyar3: Int,
        productBrfogyar4: Int,
        productCikknev: String,
        productCikkszam: String,
        productEgesz: Int,
        productPlu: Int,
        productPluszekcio: Int,
        productUtbeszar: Int
    ): AllProducts {
        return AllProducts(
            productId = productId,
            productAfaszaz = productAfaszaz,
            productBrfogyar = productBrfogyar,
            productBrfogyar2 = productBrfogyar2,
            productBrfogyar3 = productBrfogyar3,
            productBrfogyar4 = productBrfogyar4,
            productCikknev = productCikknev,
            productCikkszam = productCikkszam,
            productEgesz = productEgesz,
            productPlu = productPlu,
            productPluszekcio = productPluszekcio,
            productUtbeszar = productUtbeszar
        )
    }

    /**
     * Called to update an existing entry in the Inventory database.
     * Returns an instance of the [Item] entity class with the item info updated by the user.
     */
    private fun getUpdatedItemEntry(
        productId: Int,
        productAfaszaz: Int,
        productBrfogyar: Int,
        productBrfogyar2: Int,
        productBrfogyar3: Int,
        productBrfogyar4: Int,
        productCikknev: String,
        productCikkszam: String,
        productEgesz: Int,
        productPlu: Int,
        productPluszekcio: Int,
        productUtbeszar: Int
    ): AllProducts {
        return AllProducts(
            productId = productId,
            productAfaszaz = productAfaszaz,
            productBrfogyar = productBrfogyar,
            productBrfogyar2 = productBrfogyar2,
            productBrfogyar3 = productBrfogyar3,
            productBrfogyar4 = productBrfogyar4,
            productCikknev = productCikknev,
            productCikkszam = productCikkszam,
            productEgesz = productEgesz,
            productPlu = productPlu,
            productPluszekcio = productPluszekcio,
            productUtbeszar = productUtbeszar
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

