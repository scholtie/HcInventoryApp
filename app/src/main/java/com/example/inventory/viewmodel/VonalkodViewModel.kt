package com.example.inventory.viewmodel

import androidx.lifecycle.*
import com.example.inventory.data.*
import kotlinx.coroutines.launch

class VonalkodViewModel(private val vonalkodDao: VonalkodDao) : ViewModel() {

    // Cache all items form the database using LiveData.
    val allItems: LiveData<List<Vonalkod>> = vonalkodDao.getItems().asLiveData()

    fun retrieveMatchingAruid(vonalkod: String): LiveData<Vonalkod> {
        return vonalkodDao.searchBarcodeByAruid(vonalkod).asLiveData()
    }
}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class VonalkodViewModelFactory(private val vonalkodDao: VonalkodDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VonalkodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VonalkodViewModel(vonalkodDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}