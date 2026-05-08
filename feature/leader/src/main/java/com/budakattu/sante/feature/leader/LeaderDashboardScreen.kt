package com.budakattu.sante.feature.leader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.components.RouteBadge
import com.budakattu.sante.core.ui.theme.BarkBrown
import com.budakattu.sante.core.ui.theme.CharcoalInk

@Composable
fun LeaderDashboardScreen(
    leaderName: String,
    leaderId: String,
    leaderRoleLabel: String,
    onAddProduct: () -> Unit,
    onSignOut: () -> Unit,
) {
    HeritageScaffold(
        title = "Cooperative Command",
        subtitle = "Track inventory, supply movement, and fair-price alerts from one clear dashboard.",
    ) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ForestCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text("Leader Dashboard", style = MaterialTheme.typography.headlineMedium)
                            Text(
                                "A simple command view for your cooperative activity and profile details.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BarkBrown),
                        ) {
                            Text("Logout")
                        }
                    }
                    Surface(
                        tonalElevation = 0.dp,
                        color = BarkBrown.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Profile",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CharcoalInk,
                                )
                                OutlinedButton(
                                    onClick = {},
                                    shape = RoundedCornerShape(14.dp),
                                ) {
                                    Text("View data")
                                }
                            }
                            HorizontalDivider(color = BarkBrown.copy(alpha = 0.15f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ProfileInfoBlock(
                                    modifier = Modifier.weight(1f),
                                    label = "Name",
                                    value = leaderName,
                                )
                                ProfileInfoBlock(
                                    modifier = Modifier.weight(1f),
                                    label = "Role",
                                    value = leaderRoleLabel,
                                )
                            }
                            ProfileInfoBlock(
                                modifier = Modifier.fillMaxWidth(),
                                label = "User ID",
                                value = leaderId,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onAddProduct,
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text("Add product")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onAddProduct,
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text("Manage listings")
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                RouteBadge(label = "Pending", value = "07")
                RouteBadge(label = "MSP", value = "02 Alerts")
            }
            ForestCard {
                Text("Today's focus", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Pending sync records, MSP warnings, and incoming preorders are grouped here for quick review.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            ForestCard {
                Text("Supply overview", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Batch logging, seasonal intake, and cooperative summaries stay visible without crowding the screen.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = BarkBrown,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = CharcoalInk,
        )
    }
}
