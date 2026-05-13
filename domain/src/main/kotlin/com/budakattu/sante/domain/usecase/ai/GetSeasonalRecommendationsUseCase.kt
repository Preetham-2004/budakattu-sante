package com.budakattu.sante.domain.usecase.ai

import com.budakattu.sante.domain.model.ProductAvailability
import com.budakattu.sante.domain.model.SeasonalRecommendation
import com.budakattu.sante.domain.usecase.product.GetProductsUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSeasonalRecommendationsUseCase @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
) {
    operator fun invoke(): Flow<List<SeasonalRecommendation>> {
        return getProductsUseCase().map { products ->
            products
                .sortedWith(
                    compareByDescending<com.budakattu.sante.domain.model.Product> {
                        it.availability == ProductAvailability.PREBOOK_OPEN || it.availability == ProductAvailability.COMING_SOON
                    }.thenByDescending { it.availableStock },
                )
                .take(3)
                .map { product ->
                    val actionHint = when (product.availability) {
                        ProductAvailability.IN_STOCK -> "Order while this batch is ready."
                        ProductAvailability.PREBOOK_OPEN -> "Pre-book before the reservation limit fills."
                        ProductAvailability.COMING_SOON -> "Watch this batch and book when the window opens."
                        ProductAvailability.SOLD_OUT -> "Check back after the next harvest cycle."
                    }
                    SeasonalRecommendation(
                        title = product.name,
                        summary = "${product.categoryName} from ${product.familyName}, ${product.village}. ${product.season ?: "Year-round"} cycle.",
                        actionHint = actionHint,
                    )
                }
        }
    }
}
