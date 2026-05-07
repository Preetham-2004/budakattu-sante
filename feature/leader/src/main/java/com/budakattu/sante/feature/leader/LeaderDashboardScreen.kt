package com.budakattu.sante.feature.leader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.theme.Parchment

@Composable
fun LeaderDashboardScreen(
    onSignOut: () -> Unit,
) {
    Scaffold(containerColor = Parchment) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ForestCard {
                Text("Leader Dashboard", style = MaterialTheme.typography.headlineMedium)
                Text("Inventory, sync, supply logs, and cooperative monitoring start here.")
            }
            ForestCard {
                Text("Today's focus", style = MaterialTheme.typography.titleLarge)
                Text("Pending sync records, MSP exceptions, and incoming preorders will surface here next.")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSignOut,
            ) {
                Text("Sign out")
            }
        }
    }
}
