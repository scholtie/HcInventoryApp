package com.example.inventory.viewmodel

import androidx.lifecycle.*
import com.example.inventory.data.*
import kotlinx.coroutines.launch

class UsersViewModel(private val usersDao: UsersDao) : ViewModel() {

    fun retrieveMatchingUser(userName: String): LiveData<Users> {
        return usersDao.searchIdByUsername(userName).asLiveData()
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