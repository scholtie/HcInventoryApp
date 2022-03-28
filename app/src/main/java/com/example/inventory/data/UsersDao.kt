package com.example.inventory.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UsersDao {

    @Query("delete from users")
    suspend fun clear()

    @Query("SELECT * from users ORDER BY userId ASC")
    fun getItems(): Flow<List<Users>>

    @Query("SELECT * from users")
    suspend fun getUsers(): List<Users>

    @Query("SELECT name FROM users")
    suspend fun getAllUserNames(): List<String>

    @Query("SELECT * from users WHERE name = :userName")
    fun searchIdByUsername(userName: String): Flow<Users>


    // Specify the conflict strategy as IGNORE, when the user tries to add an
    // existing Item into the database Room ignores the conflict.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: Users)

    @Update
    suspend fun update(user: Users)

    @Delete
    suspend fun delete(user: Users)
}