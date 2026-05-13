package com.budakattu.sante.data.remote.firebase

data class TribalFamilyDocument(
    val familyId: String = "",
    val cooperativeId: String = "",
    val familyName: String = "",
    val village: String = "",
    val district: String = "",
    val forestRegion: String = "",
    val story: String = "",
    val primaryCraft: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

data class SupplyLogDocument(
    val logId: String = "",
    val cooperativeId: String = "",
    val productId: String = "",
    val productName: String = "",
    val familyId: String = "",
    val familyName: String = "",
    val batchId: String = "",
    val quantity: Long = 0L,
    val unit: String = "",
    val harvestDate: String = "",
    val originVillage: String = "",
    val notes: String = "",
    val createdAt: Long = 0L,
)

data class BatchRecordDocument(
    val batchId: String = "",
    val cooperativeId: String = "",
    val productId: String = "",
    val productName: String = "",
    val familyId: String = "",
    val familyName: String = "",
    val harvestDate: String = "",
    val originVillage: String = "",
    val quantity: Long = 0L,
    val unit: String = "",
    val status: String = "RECORDED",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
