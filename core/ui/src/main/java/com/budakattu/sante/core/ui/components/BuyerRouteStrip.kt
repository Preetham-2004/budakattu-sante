package com.budakattu.sante.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuyerRouteStrip(
    activeRoute: String,
    onNavigate: (String) -> Unit,
    marketRoute: String,
    cartRoute: String,
    profileRoute: String,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BuyerRouteButton("Market", marketRoute, activeRoute, onNavigate)
        BuyerRouteButton("Cart", cartRoute, activeRoute, onNavigate)
        BuyerRouteButton("Profile", profileRoute, activeRoute, onNavigate)
    }
}

@Composable
private fun BuyerRouteButton(
    label: String,
    route: String,
    activeRoute: String,
    onNavigate: (String) -> Unit,
) {
    val isActive = route == activeRoute
    if (isActive) {
        Button(onClick = { onNavigate(route) }) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = { onNavigate(route) }) {
            Text(label)
        }
    }
}
