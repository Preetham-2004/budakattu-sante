package com.budakattu.sante.domain.usecase.msp

import com.budakattu.sante.domain.repository.MspRepository
import javax.inject.Inject

class SeedMspDefaultsUseCase @Inject constructor(
    private val repository: MspRepository,
) {
    suspend operator fun invoke() = repository.seedDefaultsIfEmpty()
}
