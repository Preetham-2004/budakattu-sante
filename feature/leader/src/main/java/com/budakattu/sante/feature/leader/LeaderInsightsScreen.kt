package com.budakattu.sante.feature.leader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budakattu.sante.core.ui.components.LeaderScaffold
import com.budakattu.sante.core.ui.theme.*

@Composable
fun LeaderInsightsRoute(
    onBack: () -> Unit
) {
    LeaderInsightsScreen(onBack = onBack)
}

@Composable
private fun LeaderInsightsScreen(
    onBack: () -> Unit
) {
    LeaderScaffold(
        title = "Strategic Insights",
        subtitle = "Performance analytics and seasonal trend forecasting.",
        showBack = true,
        onBack = onBack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            item {
                PerformanceDashboardCard()
            }

            item {
                Text(
                    text = "HARVEST PREDICTIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = LeaderSecondary,
                    letterSpacing = 1.sp
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TrendAnalyticCard(
                        title = "Wild Forest Honey",
                        insight = "Demand spike expected (+42%) as summer bloom begins. Check batch availability.",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = LeaderPrimary
                    )

                    TrendAnalyticCard(
                        title = "Hand-woven Bamboo",
                        insight = "Steady urban interest. Focus on decorative inventory for the upcoming festival season.",
                        icon = Icons.Default.FilterVintage,
                        color = LeaderSecondary
                    )
                }
            }

            item {
                ImpactMetricsCard()
            }
        }
    }
}

@Composable
private fun PerformanceDashboardCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LeaderSurface,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, LeaderSecondary.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(LeaderHighlight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = LeaderPrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Growth Trajectory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CharcoalInk)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DataColumn("SALES", "₹42,500", "+12.4%")
                DataColumn("BUYERS", "128", "+8.2%")
                DataColumn("FULFILLMENT", "98%", "Excellent")
            }
        }
    }
}

@Composable
private fun DataColumn(label: String, value: String, change: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = LeaderPrimary)
        Text(
            change, 
            style = MaterialTheme.typography.labelSmall, 
            color = if (change.startsWith("+")) LeaderSecondary else LeaderAccent,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TrendAnalyticCard(title: String, insight: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LeaderSurface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CharcoalInk)
                Spacer(modifier = Modifier.height(4.dp))
                Text(insight, style = MaterialTheme.typography.bodySmall, color = Color.Gray, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun ImpactMetricsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LeaderSecondary,
        shadowElevation = 4.dp
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Diversity3, contentDescription = null, tint = LeaderHighlight, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text("Collective Impact", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = Color.White)
                Text(
                    "12 tribal families empowered through your cooperative this harvest cycle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
