package com.example.inventory.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AllProductsDao {
    @Query("SELECT * FROM allProducts")
    suspend fun getAll(): List<AllProducts>

    @Query("SELECT * from allProducts ORDER BY cikknev ASC")
    fun getItems(): Flow<List<AllProducts>>

    @Query("SELECT * from allProducts WHERE productId = :id")
    fun getItem(id: Int): Flow<AllProducts>

    @Query("SELECT * from allProducts WHERE productId = :productid")
    fun searchBarcode(productid: Int): Flow<AllProducts>

    @Query("delete from allProducts")
    suspend fun clear()

    // Specify the conflict strategy as IGNORE, when the user tries to add an
    // existing Item into the database Room ignores the conflict.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg allProducts: com.example.inventory.data.AllProducts)

    @Update
    suspend fun update(allProduct: AllProducts)

    @Delete
    suspend fun delete(allProduct: AllProducts)
}