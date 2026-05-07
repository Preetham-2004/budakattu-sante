package com.budakattu.sante.di

import com.budakattu.sante.data.repository.LocalSessionRepository
import com.budakattu.sante.data.repository.ProductRepositoryImpl
import com.budakattu.sante.data.repository.RoomSyncRepository
import com.budakattu.sante.data.util.AndroidNetworkMonitor
import com.budakattu.sante.domain.repository.NetworkMonitor
import com.budakattu.sante.domain.repository.ProductRepository
import com.budakattu.sante.domain.repository.SessionRepository
import com.budakattu.sante.domain.repository.SyncRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl,
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        roomSyncRepository: RoomSyncRepository,
    ): SyncRepository

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        androidNetworkMonitor: AndroidNetworkMonitor,
    ): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        localSessionRepository: LocalSessionRepository,
    ): SessionRepository
}
