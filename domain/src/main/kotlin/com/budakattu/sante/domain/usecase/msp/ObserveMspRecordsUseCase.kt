package com.budakattu.sante.domain.usecase.msp

import com.budakattu.sante.domain.repository.MspRepository
import javax.inject.Inject

class ObserveMspRecordsUseCase @Inject constructor(
    private val repository: MspRepository,
) {
    operator fun invoke() = repository.observeRecords()
}
