/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Database class with a singleton INSTANCE object.
 */
@Database(entities = [Item::class, AllProducts::class], version = 23, exportSchema = false)
abstract class ItemRoomDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun allProductsDao(): AllProductsDao
    //abstract fun importDao(): ImportDao

    companion object {
        private var INSTANCE: ItemRoomDatabase? = null
        private const val DB_NAME = "item_database"

        fun getDatabase(context: Context): ItemRoomDatabase {
            if (INSTANCE == null) {
                synchronized(ItemRoomDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            ItemRoomDatabase::class.java,
                            DB_NAME
                        )
                            //.allowMainThreadQueries() // Uncomment if you don't want to use RxJava or coroutines just yet (blocks UI thread)
                            .addCallback(object : Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    Log.d("ItemRoomDatabase", "populating with data...")
                                    //GlobalScope.launch(Dispatchers.IO) { rePopulateDb(INSTANCE) }
                                }
                            }).fallbackToDestructiveMigration().build()
                    }
                }
            }

            return INSTANCE!!
        }
    }
}