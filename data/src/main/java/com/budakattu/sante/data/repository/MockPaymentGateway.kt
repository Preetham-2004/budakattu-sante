package com.budakattu.sante.data.repository

import com.budakattu.sante.domain.repository.PaymentGateway
import com.budakattu.sante.domain.repository.PaymentResult
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

class MockPaymentGateway @Inject constructor() : PaymentGateway {
    override suspend fun initiatePayment(
        amount: Double,
        orderId: String,
        customerName: String,
        customerEmail: String,
        customerPhone: String
    ): PaymentResult {
        // Simulate network delay for payment processing
        delay(2000)
        
        // Mocking a successful payment for now
        return PaymentResult.Success(
            transactionId = "TXN_${UUID.randomUUID().toString().take(8).uppercase()}"
        )
    }
}
