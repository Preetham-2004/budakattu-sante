package com.budakattu.sante.domain.repository

import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeSession(): Flow<SessionState>
    suspend fun completeOnboarding()
    suspend fun signIn(email: String, password: String)
    suspend fun signInWithGoogle(idToken: String)
    suspend fun signUp(name: String, email: String, password: String, role: UserRole)
    suspend fun signOut()
}
