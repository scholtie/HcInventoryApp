package com.example.inventory.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AllProductsDao {
    @Query("SELECT * FROM allProducts")
    fun getAll(): List<Item>

    @Query("SELECT * from allProducts ORDER BY name ASC")
    fun getItems(): Flow<List<Item>>

    @Query("SELECT * from allProducts WHERE id = :id")
    fun getItem(id: Int): Flow<Item>

    // Specify the conflict strategy as IGNORE, when the user tries to add an
    // existing Item into the database Room ignores the conflict.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(allProduct: AllProducts)

    @Update
    suspend fun update(allProduct: AllProducts)

    @Delete
    suspend fun delete(allProduct: AllProducts)
}