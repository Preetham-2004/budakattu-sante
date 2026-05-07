package com.budakattu.sante.data.remote.firebase

import com.budakattu.sante.domain.model.Product
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toProductDomain(): Product? {
    val document = toObject(ProductDocument::class.java) ?: return null
    return Product(
        productId = document.productId.ifBlank { id },
        familyId = document.familyId,
        categoryId = document.categoryId,
        name = document.name,
        description = document.description,
        categoryName = document.categoryName,
        familyName = document.familyName,
        village = document.village,
        pricePerUnit = document.pricePerUnit.toFloat(),
        mspPerUnit = document.mspPerUnit.toFloat(),
        unit = document.unit,
        stockQty = document.stockQty.toInt(),
        isSeasonal = document.isSeasonal,
        season = document.season,
        imageUrls = document.imageUrls,
        isAvailable = document.isAvailable,
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
    categoryName = categoryName,
    familyName = familyName,
    village = village,
    pricePerUnit = pricePerUnit.toDouble(),
    mspPerUnit = mspPerUnit.toDouble(),
    unit = unit,
    stockQty = stockQty.toLong(),
    isSeasonal = isSeasonal,
    season = season,
    imageUrls = imageUrls,
    isAvailable = isAvailable,
    addedAt = addedAt,
    lastModifiedAt = lastModifiedAt,
)
