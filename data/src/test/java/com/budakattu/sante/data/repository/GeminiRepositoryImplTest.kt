package com.budakattu.sante.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiRepositoryImplTest {
    private val repository = GeminiRepositoryImpl()

    @Test
    fun chatReply_returnsImmunityGuidance_forHoneyQuestion() = runTest {
        val response = repository.getChatReply("Which honey is best for immunity?")
        assertTrue(response.contains("honey", ignoreCase = true))
        assertTrue(response.contains("immunity", ignoreCase = true))
    }

    @Test
    fun chatReply_returnsStableCachedResponse_forSamePrompt() = runTest {
        val first = repository.getChatReply("How does preorder work?")
        val second = repository.getChatReply("How does preorder work?")
        assertTrue(first == second)
    }
}
