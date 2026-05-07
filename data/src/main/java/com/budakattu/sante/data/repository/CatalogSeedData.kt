package com.budakattu.sante.data.repository

import com.budakattu.sante.domain.model.Product

object CatalogSeedData {
    fun products(): List<Product> {
        val now = System.currentTimeMillis()
        return listOf(
            Product(
                productId = "wild-honey-001",
                familyId = "family-biligiri-01",
                categoryId = "honey",
                name = "Wild Honey",
                description = "Raw forest honey collected in small batches from protected hill tracts.",
                categoryName = "Honey",
                familyName = "Jenu Kuruba Collective",
                village = "Biligiri",
                pricePerUnit = 420f,
                mspPerUnit = 380f,
                unit = "kg",
                stockQty = 18,
                isSeasonal = true,
                season = "Summer",
                imageUrls = emptyList(),
                isAvailable = true,
                addedAt = now,
                lastModifiedAt = now,
            ),
            Product(
                productId = "shikakai-002",
                familyId = "family-mm-hills-04",
                categoryId = "forest-produce",
                name = "Shikakai Pods",
                description = "Sun-dried pods cleaned and packed for hair care and herbal use.",
                categoryName = "Forest Produce",
                familyName = "Soliga Women Farmers",
                village = "MM Hills",
                pricePerUnit = 120f,
                mspPerUnit = 145f,
                unit = "kg",
                stockQty = 36,
                isSeasonal = false,
                season = null,
                imageUrls = emptyList(),
                isAvailable = true,
                addedAt = now - 1_000L,
                lastModifiedAt = now - 1_000L,
            ),
            Product(
                productId = "amla-003",
                familyId = "family-bandipur-09",
                categoryId = "fruit",
                name = "Forest Amla",
                description = "Hand-sorted amla with bright acidity, ideal for pickles and preserves.",
                categoryName = "Fruit",
                familyName = "Irula Growers Circle",
                village = "Bandipur Fringe",
                pricePerUnit = 95f,
                mspPerUnit = 90f,
                unit = "kg",
                stockQty = 42,
                isSeasonal = true,
                season = "Monsoon",
                imageUrls = emptyList(),
                isAvailable = true,
                addedAt = now - 2_000L,
                lastModifiedAt = now - 2_000L,
            ),
        )
    }
}
