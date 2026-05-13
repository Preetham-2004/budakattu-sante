package com.budakattu.sante.feature.catalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.core.ui.components.ChatMessage
import com.budakattu.sante.domain.usecase.ai.GetChatReplyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupportChatViewModel @Inject constructor(
    private val getChatReplyUseCase: GetChatReplyUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Namaste! I am your Budakattu Assistant. How can I help you today?", false))
    )
    val messages = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping = _isTyping.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text, true)
        _messages.update { it + userMessage }

        viewModelScope.launch {
            _isTyping.value = true
            delay(1000) // Natural delay
            try {
                val reply = getChatReplyUseCase(text)
                _messages.update { it + ChatMessage(reply, false) }
            } catch (_: Exception) {
                _messages.update { it + ChatMessage("I'm sorry, I'm having trouble connecting. Please try again later.", false) }
            } finally {
                _isTyping.value = false
            }
        }
    }
}
