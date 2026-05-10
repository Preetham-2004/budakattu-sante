package com.budakattu.sante.di

import com.budakattu.sante.data.repository.FirestoreOrderRepository
import com.budakattu.sante.data.repository.FirestoreProductRepository
import com.budakattu.sante.data.repository.FirebaseSessionRepository
import com.budakattu.sante.data.util.AndroidNetworkMonitor
import com.budakattu.sante.domain.repository.NetworkMonitor
import com.budakattu.sante.domain.repository.OrderRepository
import com.budakattu.sante.domain.repository.ProductRepository
import com.budakattu.sante.domain.repository.SessionRepository
import com.budakattu.sante.data.repository.FirestoreTraceabilityRepository
import com.budakattu.sante.domain.repository.TraceabilityRepository
import com.budakattu.sante.data.repository.MockPaymentGateway
import com.budakattu.sante.domain.repository.PaymentGateway
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
    abstract fun bindPaymentGateway(
        mockPaymentGateway: MockPaymentGateway,
    ): PaymentGateway

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
    abstract fun bindTraceabilityRepository(
        traceabilityRepositoryImpl: FirestoreTraceabilityRepository,
    ): TraceabilityRepository

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
