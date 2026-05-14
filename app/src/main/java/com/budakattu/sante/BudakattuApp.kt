package com.budakattu.sante

import android.app.Application
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BudakattuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Stripe with a demo publishable key
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51PzV1hP7mK6rL4R2...MOCK_KEY",
        )
    }
}
