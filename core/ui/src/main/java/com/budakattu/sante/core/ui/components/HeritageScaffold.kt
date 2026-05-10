package com.budakattu.sante.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.theme.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*

@Composable
fun HeritageScaffold(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    topBarContent: @Composable (BoxScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = TraditionalBackground,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Header Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?q=80&w=2560",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                            )
                        )
                )
                
                if (topBarContent != null) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        topBarContent()
                    }
                } else if (showBack) {
                    IconButton(
                        onClick = { onBack?.invoke() },
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 180.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = BarkBrown,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalInk.copy(alpha = 0.5f),
                        letterSpacing = 0.sp
                    )
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    content(PaddingValues(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun RouteBadge(
    label: String, 
    value: String,
    modifier: Modifier = Modifier,
    color: Color = TraditionalPrimary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = TraditionalSurface,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = CharcoalInk.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Black
            )
        }
    }
}
