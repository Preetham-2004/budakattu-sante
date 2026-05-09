package com.budakattu.sante.data.remote.firebase

import com.budakattu.sante.domain.model.Product
import com.budakattu.sante.domain.model.ProductAvailability
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toProductDomain(): Product? {
    val document = toObject(ProductDocument::class.java) ?: return null
    
    // Manually handle alternate field names if the automatic mapping missed them
    val seasonal = document.isSeasonal || getBoolean("seasonal") ?: false
    val prebookEnabled = document.isPrebookEnabled || getBoolean("prebookEnabled") ?: false
    val available = document.isAvailable && (getBoolean("available") ?: true)
    val stock = if (document.availableStock > 0) document.availableStock else getLong("stockQty") ?: document.availableStock

    return Product(
        productId = document.productId.ifBlank { id },
        familyId = document.familyId,
        categoryId = document.categoryId,
        name = document.name,
        description = document.description,
        audioDescription = document.audioDescription.ifBlank { document.description },
        categoryName = document.categoryName,
        familyName = document.familyName,
        village = document.village,
        pricePerUnit = document.pricePerUnit.toFloat(),
        mspPerUnit = document.mspPerUnit.toFloat(),
        unit = document.unit,
        availableStock = stock.toInt(),
        reservedStock = document.reservedStock.toInt(),
        soldStock = document.soldStock.toInt(),
        preorderLimit = document.preorderLimit.toInt(),
        isSeasonal = seasonal,
        season = document.season,
        imageUrls = document.imageUrls,
        availability = document.availability.toAvailability(),
        isPrebookEnabled = prebookEnabled,
        expectedDispatchDate = document.expectedDispatchDate,
        isAvailable = available,
        addedAt = document.addedAt,
        lastModifiedAt = document.lastModifiedAt,
    )
}

fun Product.toFirestoreDocument(): ProductDocument = ProductDocument(
    productId = productId,
    familyId = familyId,
    categoryId = categoryId,
    name = name,
    description = description,
    audioDescription = audioDescription,
    categoryName = categoryName,
    familyName = familyName,
    village = village,
    pricePerUnit = pricePerUnit.toDouble(),
    mspPerUnit = mspPerUnit.toDouble(),
    unit = unit,
    availableStock = availableStock.toLong(),
    reservedStock = reservedStock.toLong(),
    soldStock = soldStock.toLong(),
    preorderLimit = preorderLimit.toLong(),
    isSeasonal = isSeasonal,
    season = season,
    imageUrls = imageUrls,
    availability = availability.name,
    isPrebookEnabled = isPrebookEnabled,
    expectedDispatchDate = expectedDispatchDate,
    isAvailable = isAvailable,
    addedAt = addedAt,
    lastModifiedAt = lastModifiedAt,
)

private fun String.toAvailability(): ProductAvailability {
    return ProductAvailability.entries.firstOrNull { it.name == this }
        ?: ProductAvailability.IN_STOCK
}
