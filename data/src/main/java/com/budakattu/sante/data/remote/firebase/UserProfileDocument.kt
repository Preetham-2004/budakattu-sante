package com.budakattu.sante.data.remote.firebase

data class UserProfileDocument(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val alternatePhoneNumber: String = "",
    val address: String = "",
    val landmark: String = "",
    val city: String = "",
    val district: String = "",
    val state: String = "",
    val pincode: String = "",
    val preferredLanguage: String = "",
    val deliveryInstructions: String = "",
    val profilePictureUrl: String? = null,
    val role: String = "BUYER",
    val cooperativeId: String? = null,
    val onboardingCompleted: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
