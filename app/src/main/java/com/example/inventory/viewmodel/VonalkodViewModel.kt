package com.example.inventory.viewmodel

import androidx.lifecycle.*
import com.example.inventory.data.*
import kotlinx.coroutines.launch

class VonalkodViewModel(private val vonalkodDao: VonalkodDao) : ViewModel() {

    // Cache all items form the database using LiveData.
    val allItems: LiveData<List<Vonalkod>> = vonalkodDao.getItems().asLiveData()
    //val itemsList: LiveData<List<Item>>

    /**
     * Returns true if stock is available to sell, false otherwise.
     */

    /*init {
        itemsList = itemDao.allItems
    }*/

    /**
     * Updates an existing Item in the database.
     */
    fun updateItem(
        vonalkodAruid: Int,
        vonalkodKarton: Int,
        vonalkodVonalkod: String
    ) {
        val updatedItem = getUpdatedItemEntry(vonalkodAruid, vonalkodKarton, vonalkodVonalkod)
        updateItem(updatedItem)
    }


    /**
     * Launching a new coroutine to update an item in a non-blocking way
     */
    private fun updateItem(vonalkod: Vonalkod) {
        viewModelScope.launch {
            vonalkodDao.update(vonalkod)
        }
    }

    fun retrieveMatchingAruid(vonalkod: String): LiveData<Vonalkod> {
        return vonalkodDao.searchBarcodeByAruid(vonalkod).asLiveData()
    }

    /**
     * Decreases the stock by one unit and updates the database.
     */
    /*fun sellItem(item: Item) {
        if (item.itemMennyiseg > 0) {
            // Decrease the quantity by 1
            val newItem = item.copy(itemMennyiseg = item.itemMennyiseg - 1)
            updateItem(newItem)
        }
    }*/

    /**
     * Inserts the new Item into database.
     */
    fun addNewItem(vonalkodAruid: Int,
                   vonalkodKarton: Int,
                   vonalkodVonalkod: String) {
        val newItem = getNewItemEntry(vonalkodAruid, vonalkodKarton, vonalkodVonalkod)
        insertItem(newItem)
    }

    /**
     * Launching a new coroutine to insert an item in a non-blocking way
     */
    private fun insertItem(vonalkod: Vonalkod) {
        viewModelScope.launch {
            vonalkodDao.insert(vonalkod)
        }
    }

    fun deleteAll()
    {

    }
    /**
     * Launching a new coroutine to delete an item in a non-blocking way
     */
    fun deleteItem(vonalkod: Vonalkod) {
        viewModelScope.launch {
            vonalkodDao.delete(vonalkod)
        }
    }

    /**
     * Retrieve an item from the repository.
     */
    fun retrieveItem(id: Int): LiveData<Vonalkod> {
        return vonalkodDao.getItem(id).asLiveData()
    }



    /**
     * Returns true if the EditTexts are not empty
     */
    fun isEntryValid(
        vonalkodAruid: Int,
        vonalkodKarton: Int,
        vonalkodVonalkod: Char): Boolean {
        if (vonalkodAruid.toString().isBlank() || vonalkodKarton.toString().isBlank() || vonalkodVonalkod.toString().isBlank()){
            return false
        }
        return true
    }

    /**
     * Returns an instance of the [Item] entity class with the item info entered by the user.
     * This will be used to add a new entry to the Inventory database.
     */
    private fun getNewItemEntry(vonalkodAruid: Int,
                                vonalkodKarton: Int,
                                vonalkodBarcode: String): Vonalkod {
        return Vonalkod(
            vonalkodAruid = vonalkodAruid,
            vonalkodKarton = vonalkodKarton,
            vonalkodBarcode = vonalkodBarcode
        )
    }

    /**
     * Called to update an existing entry in the Inventory database.
     * Returns an instance of the [Item] entity class with the item info updated by the user.
     */
    private fun getUpdatedItemEntry(
        vonalkodAruid: Int,
        vonalkodKarton: Int,
        vonalkodBarcode: String
    ): Vonalkod {
        return Vonalkod(
            vonalkodAruid = vonalkodAruid,
            vonalkodKarton = vonalkodKarton,
            vonalkodBarcode = vonalkodBarcode
        )
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