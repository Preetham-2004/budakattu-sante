package com.budakattu.sante.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.budakattu.sante.data.local.converter.Converters
import com.budakattu.sante.data.local.dao.MspDao
import com.budakattu.sante.data.local.dao.ProductDao
import com.budakattu.sante.data.local.entity.MspEntity
import com.budakattu.sante.data.local.entity.ProductEntity

@Database(
    entities = [
        MspEntity::class,
        ProductEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BudakattuDatabase : RoomDatabase() {
    abstract fun mspDao(): MspDao
    abstract fun productDao(): ProductDao
}
