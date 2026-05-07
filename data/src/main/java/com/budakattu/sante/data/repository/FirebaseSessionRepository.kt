package com.budakattu.sante.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.budakattu.sante.data.local.datastore.SessionPreferences
import com.budakattu.sante.data.remote.firebase.FirestorePaths
import com.budakattu.sante.data.remote.firebase.UserProfileDocument
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.model.UserRole
import com.budakattu.sante.domain.repository.SessionRepository
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class FirebaseSessionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : SessionRepository {
    override fun observeSession(): Flow<SessionState> = callbackFlow {
        val onboardingCompleted = dataStore.data.first()[SessionPreferences.ONBOARDING_COMPLETED] ?: false
        var profileRegistration: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { auth ->
            profileRegistration?.remove()
            val user = auth.currentUser
            if (user == null) {
                trySend(SessionState.LoggedOut(onboardingCompleted = onboardingCompleted))
            } else {
                profileRegistration = firestore.collection(FirestorePaths.USERS)
                    .document(user.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        val profile = snapshot?.toObject(UserProfileDocument::class.java)
                        if (profile == null) {
                            launch {
                                bootstrapUserProfile(
                                    firebaseUser = user,
                                    onboardingCompleted = onboardingCompleted,
                                )
                            }
                            trySend(SessionState.Loading)
                        } else {
                            trySend(
                                SessionState.LoggedIn(
                                    userId = user.uid,
                                    name = profile.name.ifBlank { user.email.orEmpty() },
                                    role = profile.role.toUserRole(),
                                    onboardingCompleted = profile.onboardingCompleted,
                                ),
                            )
                        }
                    }
            }
        }

        firebaseAuth.addAuthStateListener(authListener)
        awaitClose {
            profileRegistration?.remove()
            firebaseAuth.removeAuthStateListener(authListener)
        }
    }

    override suspend fun completeOnboarding() {
        dataStore.edit { preferences ->
            preferences[SessionPreferences.ONBOARDING_COMPLETED] = true
        }
    }

    override suspend fun signIn(email: String, password: String) {
        awaitTask { firebaseAuth.signInWithEmailAndPassword(email, password) }
    }

    override suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = awaitTask { firebaseAuth.signInWithCredential(credential) }
        val firebaseUser = requireNotNull(authResult.user)
        ensureUserProfile(firebaseUser)
    }

    override suspend fun signUp(name: String, email: String, password: String, role: UserRole) {
        val authResult = awaitTask { firebaseAuth.createUserWithEmailAndPassword(email, password) }
        val firebaseUser = requireNotNull(authResult.user)
        val now = System.currentTimeMillis()
        val profile = UserProfileDocument(
            uid = firebaseUser.uid,
            name = name,
            email = email,
            role = role.name,
            cooperativeId = if (role == UserRole.LEADER) FirestorePaths.DEFAULT_COOPERATIVE_ID else null,
            onboardingCompleted = true,
            createdAt = now,
            updatedAt = now,
        )
        awaitTask {
            firestore.collection(FirestorePaths.USERS)
                .document(firebaseUser.uid)
                .set(profile)
        }
        dataStore.edit { preferences ->
            preferences[SessionPreferences.ONBOARDING_COMPLETED] = true
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        runCatching {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        }
    }

    private suspend fun <T> awaitTask(block: () -> com.google.android.gms.tasks.Task<T>): T {
        return suspendCancellableCoroutine { continuation ->
            block()
                .addOnSuccessListener { result ->
                    if (continuation.isActive) {
                        continuation.resumeWith(Result.success(result))
                    }
                }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    private suspend fun bootstrapUserProfile(
        firebaseUser: FirebaseUser,
        onboardingCompleted: Boolean,
    ) {
        val now = System.currentTimeMillis()
        val profile = UserProfileDocument(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@").orEmpty(),
            email = firebaseUser.email.orEmpty(),
            role = UserRole.BUYER.name,
            cooperativeId = null,
            onboardingCompleted = onboardingCompleted,
            createdAt = now,
            updatedAt = now,
        )
        awaitTask {
            firestore.collection(FirestorePaths.USERS)
                .document(firebaseUser.uid)
                .set(profile)
        }
    }

    private suspend fun ensureUserProfile(firebaseUser: FirebaseUser) {
        val onboardingCompleted = dataStore.data.first()[SessionPreferences.ONBOARDING_COMPLETED] ?: true
        val profileRef = firestore.collection(FirestorePaths.USERS).document(firebaseUser.uid)
        val snapshot = awaitTask { profileRef.get() }
        if (!snapshot.exists()) {
            bootstrapUserProfile(firebaseUser, onboardingCompleted)
        } else if (snapshot.getBoolean("onboardingCompleted") != true) {
            awaitTask {
                profileRef.update(
                    mapOf(
                        "onboardingCompleted" to onboardingCompleted,
                        "updatedAt" to System.currentTimeMillis(),
                    ),
                )
            }
        }
        dataStore.edit { preferences ->
            preferences[SessionPreferences.ONBOARDING_COMPLETED] = true
        }
    }

    private fun String.toUserRole(): UserRole {
        return UserRole.entries.firstOrNull { it.name == this } ?: UserRole.BUYER
    }
}
