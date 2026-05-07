package com.budakattu.sante.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.budakattu.sante.data.local.datastore.SessionPreferences
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.model.UserRole
import com.budakattu.sante.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class LocalSessionRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SessionRepository {
    override fun observeSession(): Flow<SessionState> {
        return dataStore.data.map { preferences ->
            val userId = preferences[SessionPreferences.USER_ID]
            val name = preferences[SessionPreferences.USER_NAME]
            val role = preferences[SessionPreferences.USER_ROLE]
            val onboardingCompleted = preferences[SessionPreferences.ONBOARDING_COMPLETED] ?: false

            if (userId.isNullOrBlank() || name.isNullOrBlank() || role.isNullOrBlank()) {
                SessionState.LoggedOut(onboardingCompleted = onboardingCompleted)
            } else {
                SessionState.LoggedIn(
                    userId = userId,
                    name = name,
                    role = UserRole.valueOf(role),
                    onboardingCompleted = onboardingCompleted,
                )
            }
        }
    }

    override suspend fun completeOnboarding() {
        dataStore.edit { preferences ->
            preferences[SessionPreferences.ONBOARDING_COMPLETED] = true
        }
    }

    override suspend fun signIn(name: String, role: UserRole) {
        dataStore.edit { preferences ->
            preferences[SessionPreferences.USER_ID] = UUID.randomUUID().toString()
            preferences[SessionPreferences.USER_NAME] = name
            preferences[SessionPreferences.USER_ROLE] = role.name
            preferences[SessionPreferences.ONBOARDING_COMPLETED] = true
        }
    }

    override suspend fun signOut() {
        dataStore.edit { preferences ->
            preferences.remove(SessionPreferences.USER_ID)
            preferences.remove(SessionPreferences.USER_NAME)
            preferences.remove(SessionPreferences.USER_ROLE)
        }
    }
}
