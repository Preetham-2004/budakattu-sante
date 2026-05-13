package com.budakattu.sante.data.repository

import com.budakattu.sante.data.BuildConfig
import com.budakattu.sante.domain.repository.AiRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class GeminiRepositoryImpl @Inject constructor() : AiRepository {
    private val responseCache = ConcurrentHashMap<String, String>()

    override suspend fun getChatReply(userMessage: String): String {
        val normalized = userMessage.trim()
        val cacheKey = normalized.lowercase()
        responseCache[cacheKey]?.let { return it }

        // System Instruction: "You are a helpful assistant for Budakattu Sante. 
        // Always respond in the same language that the user uses."
        
        val response = if (BuildConfig.GEMINI_API_KEY == "YOUR_GEMINI_API_KEY_HERE") {
            localReply(normalized.lowercase())
        } else {
            // When using actual Gemini API, the prompt would be:
            // "Respond in the same language as: $normalized"
            localReply(normalized.lowercase())
        }
        responseCache[cacheKey] = response
        return response
    }

    private fun localReply(message: String): String {
        return when {
            "immunity" in message || "best honey" in message ->
                "Wild forest honey is usually the strongest fit for immunity-focused buyers. Check the batch season and pre-book early when harvest is limited."
            "harvest" in message || "season" in message ->
                "Harvest timing depends on bloom cycles, bamboo craft runs, and forest access windows. Seasonal products are best pre-booked before the dispatch window opens."
            "use" in message || "how to" in message ->
                "Use the product detail page for audio guidance, source details, and pricing. For herbal produce, start with small household quantities until you know your preferred batch."
            "prebook" in message || "pre-order" in message || "preorder" in message ->
                "Pre-booking reserves your place before the cooperative completes production or harvest. Reserved orders move to confirmed once the batch is ready."
            else ->
                "Budakattu-Sante connects buyers to tribal cooperative products with MSP visibility, preorder support, and source transparency. Ask about honey, harvest timing, usage tips, or preorder timing."
        }
    }
}
