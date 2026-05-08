package com.budakattu.sante.di

import com.budakattu.sante.data.repository.FirestoreOrderRepository
import com.budakattu.sante.data.repository.FirestoreProductRepository
import com.budakattu.sante.data.repository.FirebaseSessionRepository
import com.budakattu.sante.data.util.AndroidNetworkMonitor
import com.budakattu.sante.domain.repository.NetworkMonitor
import com.budakattu.sante.domain.repository.OrderRepository
import com.budakattu.sante.domain.repository.ProductRepository
import com.budakattu.sante.domain.repository.SessionRepository
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
        productRepositoryImpl: FirestoreProductRepository,
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: FirestoreOrderRepository,
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        androidNetworkMonitor: AndroidNetworkMonitor,
    ): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        firebaseSessionRepository: FirebaseSessionRepository,
    ): SessionRepository
}
