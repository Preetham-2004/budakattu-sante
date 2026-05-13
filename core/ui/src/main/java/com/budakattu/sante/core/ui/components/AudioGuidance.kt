package com.budakattu.sante.core.ui.components

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.budakattu.sante.core.ui.theme.ForestPrimary
import java.util.Locale

@Composable
fun AudioGuidanceButton(
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = ForestPrimary,
    backgroundColor: Color = Color.White.copy(alpha = 0.9f)
) {
    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialize language
            }
        }
        ttsInstance.language = Locale.getDefault()
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    IconButton(
        onClick = {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        },
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Read aloud",
            tint = tint
        )
    }
}
