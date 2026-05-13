package com.budakattu.sante.domain.usecase.session

import com.budakattu.sante.domain.repository.SessionRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(
        name: String,
        phoneNumber: String,
        alternatePhoneNumber: String,
        address: String,
        landmark: String,
        city: String,
        district: String,
        state: String,
        pincode: String,
        preferredLanguage: String,
        deliveryInstructions: String,
    ) = sessionRepository.updateProfile(
        name = name,
        phoneNumber = phoneNumber,
        alternatePhoneNumber = alternatePhoneNumber,
        address = address,
        landmark = landmark,
        city = city,
        district = district,
        state = state,
        pincode = pincode,
        preferredLanguage = preferredLanguage,
        deliveryInstructions = deliveryInstructions,
    )
}
