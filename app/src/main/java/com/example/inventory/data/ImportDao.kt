package com.example.inventory.data

import androidx.room.*

@Dao
interface ImportDao {
    @Query("delete from import")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(import: Import)

    @Query("select * from import")
    suspend fun all(): List<Import>

    @Update
    suspend fun update(import: Import)
}