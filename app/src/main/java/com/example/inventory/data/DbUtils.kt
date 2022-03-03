package com.example.inventory.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun rePopulateDb(database: ItemRoomDatabase?) {
    database?.let { db ->
        withContext(Dispatchers.IO) {
            val allProductsDao: AllProductsDao = db.allProductsDao()

            allProductsDao.clear()

            val productOne = AllProducts(productName = "Product1", productQuantityInStock = 6, productBarcode = "testest", productPrice = 5000.0)
            val productTwo = AllProducts(productName = "Product2", productQuantityInStock = 9, productBarcode = "testest2", productPrice = 7000.0)
            val productThree = AllProducts(productName = "Product3", productQuantityInStock = 16, productBarcode = "testest3", productPrice = 15000.0)
            val productFour = AllProducts(productName = "Product4", productQuantityInStock = 60, productBarcode = "testest4", productPrice = 2300.0)
            allProductsDao.insert(productOne, productTwo, productThree, productFour)
        }
    }
}