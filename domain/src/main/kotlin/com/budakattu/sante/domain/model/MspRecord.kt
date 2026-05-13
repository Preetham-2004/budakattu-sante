package com.budakattu.sante.domain.model

data class MspRecord(
    val recordId: String,
    val categoryId: String,
    val categoryName: String,
    val minimumPrice: Float,
    val district: String,
    val updatedAt: Long,
)
