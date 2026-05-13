package com.budakattu.sante.domain.usecase.ai

import com.budakattu.sante.domain.repository.AiRepository
import javax.inject.Inject

class GetChatReplyUseCase @Inject constructor(
    private val repository: AiRepository,
) {
    suspend operator fun invoke(userMessage: String): String = repository.getChatReply(userMessage)
}
