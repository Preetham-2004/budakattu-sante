package com.budakattu.sante.domain.usecase.product

import com.budakattu.sante.domain.model.Product
import com.budakattu.sante.domain.model.ProductAvailability
import com.budakattu.sante.domain.model.ProductDraft
import com.budakattu.sante.domain.repository.ProductRepository
import java.util.UUID
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke(draft: ProductDraft) {
        val now = System.currentTimeMillis()
        val product = Product(
            productId = buildProductId(draft.name, now),
            familyId = buildFamilyId(draft.familyName),
            categoryId = draft.categoryId,
            name = draft.name.trim(),
            description = draft.description.trim(),
            audioDescription = draft.audioDescription.ifBlank { draft.description }.trim(),
            categoryName = draft.categoryName.trim(),
            familyName = draft.familyName.trim(),
            village = draft.village.trim(),
            pricePerUnit = draft.pricePerUnit,
            mspPerUnit = draft.mspPerUnit,
            unit = draft.unit.trim(),
            stockQty = draft.stockQty,
            isSeasonal = !draft.season.isNullOrBlank(),
            season = draft.season?.trim()?.takeIf { it.isNotBlank() },
            imageUrls = draft.imageUrls,
            availability = draft.availability,
            isPrebookEnabled = draft.isPrebookEnabled,
            expectedDispatchDate = draft.expectedDispatchDate?.trim()?.takeIf { it.isNotBlank() },
            maxPrebookQuantity = draft.maxPrebookQuantity,
            currentPrebookCount = 0,
            isAvailable = draft.availability != ProductAvailability.SOLD_OUT,
            addedAt = now,
            lastModifiedAt = now,
        )
        productRepository.upsertProduct(product)
    }

    private fun buildProductId(name: String, now: Long): String {
        val slug = name
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { UUID.randomUUID().toString().take(8) }
        return "$slug-$now"
    }

    private fun buildFamilyId(familyName: String): String {
        return familyName
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "family-unknown" }
    }
}
