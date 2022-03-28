package com.example.inventory.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.inventory.data.Leltarhely
import com.example.inventory.data.LeltarhelyDao
import com.example.inventory.data.Users
import com.example.inventory.data.UsersDao

class LeltarhelyViewModel(private val leltarhelyDao: LeltarhelyDao) : ViewModel() {

    fun retrieveMatchingLeltarhely(leltarhelyName: String): LiveData<Leltarhely> {
        return leltarhelyDao.searchIdByLeltarhely(leltarhelyName).asLiveData()
    }
}

    class LeltarhelyViewModelFactory(private val leltarhelyDao: LeltarhelyDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LeltarhelyViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LeltarhelyViewModel(leltarhelyDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }

}