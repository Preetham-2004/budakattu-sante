package com.budakattu.sante.data.repository

import com.budakattu.sante.domain.repository.PaymentGateway
import com.budakattu.sante.domain.repository.PaymentResult
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

/**
 * A legit-looking Stripe implementation.
 * In a real production app, this would call your backend to create a PaymentIntent.
 * Then the client would use the returned clientSecret with Stripe SDK's PaymentSheet.
 */
class StripePaymentGateway @Inject constructor() : PaymentGateway {
    override suspend fun initiatePayment(
        amount: Double,
        orderId: String,
        customerName: String,
        customerEmail: String,
        customerPhone: String,
    ): PaymentResult {
        // 1. In a real app, you call:
        // val response = api.createPaymentIntent(amount, currency = "inr", orderId)
        // val clientSecret = response.clientSecret
        
        // 2. Then you'd trigger the PaymentSheet in the UI.
        
        // For this "legit" integration, we simulate the backend roundtrip 
        // and provide a mock successful transaction that mimics Stripe's behavior.
        delay(1500) 
        
        // We'll return a Success result which the ViewModel will use to confirm the order.
        return PaymentResult.Success(
            transactionId = "pi_${UUID.randomUUID().toString().replace("-", "").take(24)}",
        )
    }
}
