package com.budakattu.sante.domain.model

data class TribalFamily(
    val familyId: String,
    val cooperativeId: String,
    val familyName: String,
    val village: String,
    val district: String,
    val forestRegion: String,
    val story: String,
    val primaryCraft: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

data class SupplyLog(
    val logId: String,
    val cooperativeId: String,
    val productId: String,
    val productName: String,
    val familyId: String,
    val familyName: String,
    val batchId: String,
    val quantity: Int,
    val unit: String,
    val harvestDate: String,
    val originVillage: String,
    val notes: String,
    val createdAt: Long,
)

data class BatchRecord(
    val batchId: String,
    val cooperativeId: String,
    val productId: String,
    val productName: String,
    val familyId: String,
    val familyName: String,
    val harvestDate: String,
    val originVillage: String,
    val quantity: Int,
    val unit: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
)
