package com.budakattu.sante.domain.repository

import com.budakattu.sante.domain.model.MspRecord
import kotlinx.coroutines.flow.Flow

interface MspRepository {
    fun observeRecords(): Flow<List<MspRecord>>
    fun observeRecord(categoryId: String): Flow<MspRecord?>
    suspend fun seedDefaultsIfEmpty()
    suspend fun sync()
}
