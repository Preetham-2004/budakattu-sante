package com.budakattu.sante.data.remote.firebase

import com.budakattu.sante.domain.model.BatchRecord
import com.budakattu.sante.domain.model.SupplyLog
import com.budakattu.sante.domain.model.TribalFamily
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toTribalFamilyDomain(): TribalFamily? {
    val document = toObject(TribalFamilyDocument::class.java) ?: return null
    return TribalFamily(
        familyId = document.familyId.ifBlank { id },
        cooperativeId = document.cooperativeId,
        familyName = document.familyName,
        village = document.village,
        district = document.district,
        forestRegion = document.forestRegion,
        story = document.story,
        primaryCraft = document.primaryCraft,
        isActive = document.isActive,
        createdAt = document.createdAt,
        updatedAt = document.updatedAt,
    )
}

fun TribalFamily.toDocument(): TribalFamilyDocument = TribalFamilyDocument(
    familyId = familyId,
    cooperativeId = cooperativeId,
    familyName = familyName,
    village = village,
    district = district,
    forestRegion = forestRegion,
    story = story,
    primaryCraft = primaryCraft,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun DocumentSnapshot.toSupplyLogDomain(): SupplyLog? {
    val document = toObject(SupplyLogDocument::class.java) ?: return null
    return SupplyLog(
        logId = document.logId.ifBlank { id },
        cooperativeId = document.cooperativeId,
        productId = document.productId,
        productName = document.productName,
        familyId = document.familyId,
        familyName = document.familyName,
        batchId = document.batchId,
        quantity = document.quantity.toInt(),
        unit = document.unit,
        harvestDate = document.harvestDate,
        originVillage = document.originVillage,
        notes = document.notes,
        createdAt = document.createdAt,
    )
}

fun DocumentSnapshot.toBatchRecordDomain(): BatchRecord? {
    val document = toObject(BatchRecordDocument::class.java) ?: return null
    return BatchRecord(
        batchId = document.batchId.ifBlank { id },
        cooperativeId = document.cooperativeId,
        productId = document.productId,
        productName = document.productName,
        familyId = document.familyId,
        familyName = document.familyName,
        harvestDate = document.harvestDate,
        originVillage = document.originVillage,
        quantity = document.quantity.toInt(),
        unit = document.unit,
        status = document.status,
        createdAt = document.createdAt,
        updatedAt = document.updatedAt,
    )
}
