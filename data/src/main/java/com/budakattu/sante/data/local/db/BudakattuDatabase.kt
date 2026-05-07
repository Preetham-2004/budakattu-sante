package com.budakattu.sante.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.budakattu.sante.data.local.converter.StringListConverter
import com.budakattu.sante.data.local.dao.ProductDao
import com.budakattu.sante.data.local.dao.SyncQueueDao
import com.budakattu.sante.data.local.entity.ProductEntity
import com.budakattu.sante.data.local.entity.SyncQueueEntity

@Database(
    entities = [ProductEntity::class, SyncQueueEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(StringListConverter::class)
abstract class BudakattuDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var instance: BudakattuDatabase? = null

        fun getInstance(context: Context): BudakattuDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BudakattuDatabase::class.java,
                    "budakattu.db",
                ).build().also { instance = it }
            }
        }
    }
}
