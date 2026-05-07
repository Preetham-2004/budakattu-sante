package com.budakattu.sante.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budakattu.sante.core.ui.theme.MspDanger
import com.budakattu.sante.core.ui.theme.MspSafe

@Composable
fun MspBadge(
    isSafe: Boolean,
    modifier: Modifier = Modifier,
) {
    val background = if (isSafe) MspSafe else MspDanger
    val label = if (isSafe) "MSP Safe" else "Below MSP"
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}
