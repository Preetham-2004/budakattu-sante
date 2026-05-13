package com.budakattu.sante.domain.usecase.msp

import com.budakattu.sante.domain.repository.MspRepository
import javax.inject.Inject

class ObserveMspRecordUseCase @Inject constructor(
    private val repository: MspRepository,
) {
    operator fun invoke(categoryId: String) = repository.observeRecord(categoryId)
}
