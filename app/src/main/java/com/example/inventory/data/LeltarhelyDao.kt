package com.example.inventory.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LeltarhelyDao {

    @Query("delete from leltarhely")
    suspend fun clear()

    @Query("SELECT name FROM leltarhely")
    suspend fun getAllLeltarhely(): List<String>

    @Query("SELECT * from leltarhely")
    suspend fun getLeltarhelyek(): List<Leltarhely>

    @Query("SELECT * from leltarhely WHERE name = :leltarhelyName")
    fun searchIdByLeltarhely(leltarhelyName: String): Flow<Leltarhely>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(leltarhely: Leltarhely)
}