package com.budakattu.sante

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BudakattuApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
