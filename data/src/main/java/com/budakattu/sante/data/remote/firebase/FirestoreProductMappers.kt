package com.budakattu.sante.data.remote.firebase

import com.budakattu.sante.domain.model.Product
import com.budakattu.sante.domain.model.ProductAvailability
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toProductDomain(): Product? {
    val document = toObject(ProductDocument::class.java)
    
    val productId = document?.productId?.takeIf { it.isNotBlank() } ?: getString("productId") ?: id
    val name = document?.name?.takeIf { it.isNotBlank() } ?: getString("name") ?: getString("productName") ?: id
    
    // Manually handle alternate field names if the automatic mapping missed them
    val seasonal = document?.isSeasonal ?: getBoolean("isSeasonal") ?: getBoolean("seasonal") ?: false
    val prebookEnabled = document?.isPrebookEnabled ?: getBoolean("isPrebookEnabled") ?: getBoolean("prebookEnabled") ?: false
    val available = document?.isAvailable ?: getBoolean("isAvailable") ?: getBoolean("available") ?: true
    
    fun getNumber(field: String): Double {
        return try {
            val value = get(field)
            when (value) {
                is Number -> value.toDouble()
                is String -> value.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    val price = document?.pricePerUnit ?: getNumber("pricePerUnit")
    val msp = document?.mspPerUnit ?: getNumber("mspPerUnit")
    val stock = document?.availableStock ?: getNumber("availableStock").toLong()
    val reserved = document?.reservedStock ?: getNumber("reservedStock").toLong()
    val sold = document?.soldStock ?: getNumber("soldStock").toLong()
    val limit = document?.preorderLimit ?: getNumber("preorderLimit").toLong()

    return Product(
        productId = productId,
        familyId = document?.familyId ?: getString("familyId") ?: "unknown",
        categoryId = document?.categoryId ?: getString("categoryId") ?: "unknown",
        name = name,
        description = document?.description ?: getString("description").orEmpty(),
        audioDescription = document?.audioDescription ?: getString("audioDescription") ?: getString("description").orEmpty(),
        categoryName = document?.categoryName ?: getString("categoryName") ?: getString("category") ?: "Produce",
        familyName = document?.familyName ?: getString("familyName") ?: "Local Collective",
        village = document?.village ?: getString("village").orEmpty(),
        pricePerUnit = price.toFloat(),
        mspPerUnit = msp.toFloat(),
        unit = document?.unit ?: getString("unit") ?: "kg",
        availableStock = stock.toInt(),
        reservedStock = reserved.toInt(),
        soldStock = sold.toInt(),
        preorderLimit = limit.toInt(),
        isSeasonal = seasonal,
        season = document?.season ?: getString("season"),
        imageUrls = document?.imageUrls ?: (get("imageUrls") as? List<String>).orEmpty(),
        availability = (document?.availability ?: getString("availability") ?: "IN_STOCK").toAvailability(),
        isPrebookEnabled = prebookEnabled,
        expectedDispatchDate = document?.expectedDispatchDate ?: getString("expectedDispatchDate"),
        isAvailable = available,
        isDraft = document?.isDraft ?: getBoolean("isDraft") ?: getBoolean("draft") ?: false,
        addedAt = document?.addedAt ?: getLong("addedAt") ?: 0L,
        lastModifiedAt = document?.lastModifiedAt ?: getLong("lastModifiedAt") ?: 0L,
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
    isDraft = isDraft,
    addedAt = addedAt,
    lastModifiedAt = lastModifiedAt,
)

private fun String.toAvailability(): ProductAvailability {
    return ProductAvailability.entries.firstOrNull { it.name == this }
        ?: ProductAvailability.IN_STOCK
}
