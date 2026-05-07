package com.budakattu.sante.data.mapper

import com.budakattu.sante.data.local.entity.ProductEntity
import com.budakattu.sante.data.local.entity.SyncQueueEntity
import com.budakattu.sante.domain.model.Product
import com.budakattu.sante.domain.model.SyncQueueItem

fun ProductEntity.toDomain(): Product = Product(
    productId = productId,
    familyId = familyId,
    categoryId = categoryId,
    name = name,
    description = description,
    categoryName = categoryName,
    familyName = familyName,
    village = village,
    pricePerUnit = pricePerUnit,
    mspPerUnit = mspPerUnit,
    unit = unit,
    stockQty = stockQty,
    isSeasonal = isSeasonal,
    season = season,
    imageUrls = imageUrls,
    isAvailable = isAvailable,
    addedAt = addedAt,
    lastModifiedAt = lastModifiedAt,
)

fun Product.toEntity(pendingSync: Boolean = false): ProductEntity = ProductEntity(
    productId = productId,
    familyId = familyId,
    categoryId = categoryId,
    name = name,
    description = description,
    categoryName = categoryName,
    familyName = familyName,
    village = village,
    pricePerUnit = pricePerUnit,
    mspPerUnit = mspPerUnit,
    unit = unit,
    stockQty = stockQty,
    isSeasonal = isSeasonal,
    season = season,
    imageUrls = imageUrls,
    isAvailable = isAvailable,
    addedAt = addedAt,
    pendingSync = pendingSync,
    lastModifiedAt = lastModifiedAt,
)

fun SyncQueueEntity.toDomain(): SyncQueueItem = SyncQueueItem(
    queueId = queueId,
    entityType = entityType,
    entityId = entityId,
    operation = operation,
    status = status,
    payload = payload,
    retryCount = retryCount,
    createdAt = createdAt,
    lastAttemptAt = lastAttemptAt,
)
