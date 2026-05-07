package com.budakattu.sante.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SessionPreferences {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val USER_ID = stringPreferencesKey("user_id")
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_ROLE = stringPreferencesKey("user_role")
}
