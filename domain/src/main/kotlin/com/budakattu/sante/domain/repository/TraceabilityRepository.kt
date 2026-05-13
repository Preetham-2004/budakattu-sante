package com.budakattu.sante.domain.repository

import com.budakattu.sante.domain.model.BatchRecord
import com.budakattu.sante.domain.model.SupplyLog
import com.budakattu.sante.domain.model.TribalFamily
import kotlinx.coroutines.flow.Flow

interface TraceabilityRepository {
    fun observeFamilies(cooperativeId: String): Flow<List<TribalFamily>>
    fun observeFamily(familyId: String): Flow<TribalFamily?>
    fun observeSupplyLogs(cooperativeId: String): Flow<List<SupplyLog>>
    fun observeSupplyLogsForProduct(productId: String): Flow<List<SupplyLog>>
    fun observeBatchRecordsForProduct(productId: String): Flow<List<BatchRecord>>
    suspend fun upsertFamily(family: TribalFamily)
    suspend fun createSupplyLog(
        cooperativeId: String,
        productId: String,
        productName: String,
        family: TribalFamily,
        quantity: Int,
        unit: String,
        harvestDate: String,
        originVillage: String,
        notes: String,
    )
}
