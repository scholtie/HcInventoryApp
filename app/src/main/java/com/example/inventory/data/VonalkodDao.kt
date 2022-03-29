package com.example.inventory.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VonalkodDao {

    @get:Query("SELECT * FROM vonalkod ORDER BY vonalkodAruid ASC")
    val allItems: LiveData<List<Vonalkod>>

    @Query("delete from vonalkod")
    suspend fun clear()

    @Query("select count(barcode) from vonalkod")
    suspend fun getCount(): Int

    @Query("SELECT * FROM vonalkod")
    suspend fun getAll(): List<Vonalkod>

    @Query("SELECT * from vonalkod ORDER BY vonalkodAruid ASC")
    fun getItems(): Flow<List<Vonalkod>>

    @Query("SELECT * from vonalkod WHERE vonalkodAruid = :vonalkodAruid")
    fun getItem(vonalkodAruid: Int): Flow<Vonalkod>

    @Query("SELECT * from vonalkod WHERE barcode = :vonalkod")
    fun searchBarcodeByAruid(vonalkod: String): Flow<Vonalkod>



    // Specify the conflict strategy as IGNORE, when the user tries to add an
    // existing Item into the database Room ignores the conflict.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vonalkod: Vonalkod)

    @Update
    suspend fun update(vonalkod: Vonalkod)

    @Delete
    suspend fun delete(vonalkod: Vonalkod)

    @Query("select * from vonalkod where karton is not null")
    suspend fun findLeltarozott(): List<Vonalkod>

    /*@Query("SELECT * from item WHERE name = :name AND barcode = :barcode")
    fun getItemByNameAndBarcode(name: string, barcode: string): List<Item>*/
}