package com.budakattu.sante.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budakattu.sante.core.ui.theme.AmberHarvest
import com.budakattu.sante.core.ui.theme.BarkBrown
import com.budakattu.sante.core.ui.theme.CharcoalInk
import com.budakattu.sante.core.ui.theme.LeafAccent
import com.budakattu.sante.core.ui.theme.MilletGold
import com.budakattu.sante.core.ui.theme.Parchment

@Composable
fun HeritageScaffold(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = Parchment,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Parchment, MilletGold.copy(alpha = 0.18f), Parchment),
                    ),
                ),
        ) {
            DecorativePattern()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Surface(
                    color = BarkBrown.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                    tonalElevation = 10.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            RouteBead(LeafAccent)
                            RouteBead(MilletGold)
                            RouteBead(AmberHarvest)
                            Text(
                                text = "BUDAKATTU SANTE",
                                style = MaterialTheme.typography.labelLarge,
                                color = Parchment,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Parchment,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Parchment.copy(alpha = 0.88f),
                        )
                    }
                }
                content(PaddingValues(horizontal = 18.dp, vertical = 18.dp))
            }
        }
    }
}

@Composable
fun RouteBadge(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BarkBrown.copy(alpha = 0.08f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = BarkBrown,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = CharcoalInk,
            )
        }
    }
}

@Composable
private fun DecorativePattern() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 128.dp, start = 18.dp, end = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(3) { index ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                repeat(10) { bead ->
                    val alpha = if ((index + bead) % 2 == 0) 0.16f else 0.08f
                    Box(
                        modifier = Modifier
                            .size(if (bead % 3 == 0) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(BarkBrown.copy(alpha = alpha)),
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteBead(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color),
    )
}
