package com.budakattu.sante.domain.usecase.family

import com.budakattu.sante.domain.model.TribalFamily
import com.budakattu.sante.domain.repository.TraceabilityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFamilyUseCase @Inject constructor(
    private val traceabilityRepository: TraceabilityRepository
) {
    operator fun invoke(familyId: String): Flow<TribalFamily?> =
        traceabilityRepository.observeFamily(familyId)
}
