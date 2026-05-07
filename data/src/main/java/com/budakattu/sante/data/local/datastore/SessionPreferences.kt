package com.budakattu.sante.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SessionPreferences {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
}
