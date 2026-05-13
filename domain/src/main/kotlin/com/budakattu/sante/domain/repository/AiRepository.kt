package com.budakattu.sante.domain.repository

interface AiRepository {
    suspend fun getChatReply(userMessage: String): String
}
