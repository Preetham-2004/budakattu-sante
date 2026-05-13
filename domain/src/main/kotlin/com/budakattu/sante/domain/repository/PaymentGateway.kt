package com.budakattu.sante.domain.repository

interface PaymentGateway {
    suspend fun initiatePayment(
        amount: Double,
        orderId: String,
        customerName: String,
        customerEmail: String,
        customerPhone: String
    ): PaymentResult
}

sealed interface PaymentResult {
    data class Success(val transactionId: String) : PaymentResult
    data class Failure(val message: String) : PaymentResult
    data object Cancelled : PaymentResult
}
