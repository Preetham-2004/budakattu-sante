package com.budakattu.sante.data.remote.firebase

data class UserProfileDocument(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "BUYER",
    val cooperativeId: String? = null,
    val onboardingCompleted: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
