package com.budakattu.sante.data.remote.firebase

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ProductDocument(
    var productId: String = "",
    var familyId: String = "",
    var categoryId: String = "",
    var name: String = "",
    var description: String = "",
    var audioDescription: String = "",
    var categoryName: String = "",
    var familyName: String = "",
    var village: String = "",
    var pricePerUnit: Double = 0.0,
    var mspPerUnit: Double = 0.0,
    var unit: String = "kg",
    var availableStock: Long = 0,
    var reservedStock: Long = 0,
    var soldStock: Long = 0,
    var preorderLimit: Long = 0,
    var isSeasonal: Boolean = false,
    var season: String? = null,
    var imageUrls: List<String> = emptyList(),
    var availability: String = "IN_STOCK",
    var isPrebookEnabled: Boolean = false,
    var expectedDispatchDate: String? = null,
    var isAvailable: Boolean = true,
    var addedAt: Long = 0L,
    var lastModifiedAt: Long = 0L,
)
