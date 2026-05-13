package com.budakattu.sante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.budakattu.sante.domain.model.MspRecord

@Entity(tableName = "msp_records")
data class MspEntity(
    @PrimaryKey
    val recordId: String,
    val categoryId: String,
    val categoryName: String,
    val minimumPrice: Float,
    val district: String,
    val updatedAt: Long,
)

fun MspEntity.asExternalModel() = MspRecord(
    recordId = recordId,
    categoryId = categoryId,
    categoryName = categoryName,
    minimumPrice = minimumPrice,
    district = district,
    updatedAt = updatedAt,
)

fun MspRecord.asEntity() = MspEntity(
    recordId = recordId,
    categoryId = categoryId,
    categoryName = categoryName,
    minimumPrice = minimumPrice,
    district = district,
    updatedAt = updatedAt,
)
