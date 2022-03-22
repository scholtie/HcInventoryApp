package com.example.inventory.viewmodel

import androidx.lifecycle.*
import com.example.inventory.data.*
import kotlinx.coroutines.launch

class UsersViewModel(private val usersDao: UsersDao) : ViewModel() {

    // Cache all items form the database using LiveData.
    val allItems: LiveData<List<Users>> = usersDao.getItems().asLiveData()
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
    /*fun updateItem(
        userId: Int,
        userName: String,
        userPass: String
    ) {
        val updatedItem = getUpdatedItemEntry(userId = userId,
            userName = userName,
            userPass = userPass)
        updateItem(updatedItem)
    }*/

    fun ListUsers() {
        viewModelScope.launch {
            usersDao.getAllUserNames()
        }
    }


    /**
     * Launching a new coroutine to update an item in a non-blocking way
     */
    /*private fun updateItem(vonalkod: Vonalkod) {
        viewModelScope.launch {
            vonalkodDao.update(vonalkod)
        }
    }*/

    fun retrieveMatchingUser(userName: String): LiveData<Users> {
        return usersDao.searchIdByUsername(userName).asLiveData()
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
    /*fun addNewItem(userId: Int,
                   userName: String,
                   userPass: String) {
        val newItem = getNewItemEntry(vonalkodAruid, vonalkodKarton, vonalkodVonalkod)
        insertItem(newItem)
    }*/

    /**
     * Launching a new coroutine to insert an item in a non-blocking way
     */
    /*private fun insertItem(vonalkod: Vonalkod) {
        viewModelScope.launch {
            vonalkodDao.insert(vonalkod)
        }
    }*/

    fun deleteAll()
    {

    }
    /**
     * Launching a new coroutine to delete an item in a non-blocking way
     */
    /*fun deleteItem(vonalkod: Vonalkod) {
        viewModelScope.launch {
            vonalkodDao.delete(vonalkod)
        }
    }*/

    /**
     * Retrieve an item from the repository.
     */
    /*fun retrieveItem(id: Int): LiveData<Vonalkod> {
        return vonalkodDao.getItem(id).asLiveData()
    }*/



    /**
     * Returns true if the EditTexts are not empty
     */
    fun isEntryValid(
        userId: Int,
        userName: String,
        userPass: String): Boolean {
        if (userId.toString().isBlank() || userName.isBlank() || userPass.isBlank()){
            return false
        }
        return true
    }

    /**
     * Returns an instance of the [Item] entity class with the item info entered by the user.
     * This will be used to add a new entry to the Inventory database.
     */
    private fun getNewItemEntry(userId: Int,
                                userName: String,
                                userPass: String): Users {
        return Users(
            userId = userId,
            userName = userName,
            userPass = userPass
        )
    }

    /**
     * Called to update an existing entry in the Inventory database.
     * Returns an instance of the [Item] entity class with the item info updated by the user.
     */
    private fun getUpdatedItemEntry(
        userId: Int,
        userName: String,
        userPass: String
    ): Users {
        return Users(
            userId = userId,
            userName = userName,
            userPass = userPass
        )
    }
}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class UsersViewModelFactory(private val usersDao: UsersDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UsersViewModel(usersDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}