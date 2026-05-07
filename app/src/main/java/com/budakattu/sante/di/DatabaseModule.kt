package com.budakattu.sante.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.budakattu.sante.data.local.dao.ProductDao
import com.budakattu.sante.data.local.dao.SyncQueueDao
import com.budakattu.sante.data.local.db.BudakattuDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): BudakattuDatabase {
        return Room.databaseBuilder(
            context,
            BudakattuDatabase::class.java,
            "budakattu.db",
        ).build()
    }

    @Provides
    fun provideProductDao(database: BudakattuDatabase): ProductDao = database.productDao()

    @Provides
    fun provideSyncQueueDao(database: BudakattuDatabase): SyncQueueDao = database.syncQueueDao()

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("budakattu_session.preferences_pb") },
        )
    }
}
