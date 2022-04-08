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

package com.example.inventory.viewmodel

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


    fun retrieveMatchingBarcode(productid: Int): LiveData<AllProducts> {
        return allProductsDao.searchBarcode(productid).asLiveData()
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

