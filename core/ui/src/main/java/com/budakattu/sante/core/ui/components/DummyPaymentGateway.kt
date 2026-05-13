package com.budakattu.sante.core.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.budakattu.sante.core.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun DummyPaymentDialog(
    amount: String,
    onPaymentComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    var stage by remember { mutableStateOf(PaymentStage.PROCESSING) }

    LaunchedEffect(Unit) {
        delay(3000) // Simulate processing
        stage = PaymentStage.SUCCESS
        delay(1500) // Show success state briefly
        onPaymentComplete()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = ForestPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SECURE GATEWAY", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }

                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (stage) {
                        PaymentStage.PROCESSING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = ForestPrimary,
                                strokeWidth = 6.dp
                            )
                        }
                        PaymentStage.SUCCESS -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MspSafe,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                        PaymentStage.FAILED -> {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = LeaderError,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (stage) {
                            PaymentStage.PROCESSING -> "Authorizing Payment"
                            PaymentStage.SUCCESS -> "Payment Successful!"
                            PaymentStage.FAILED -> "Payment Failed"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalInk
                    )
                    Text(
                        text = "Amount: $amount",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ForestPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (stage == PaymentStage.PROCESSING) {
                    Text(
                        text = "Powered by Razorpay (Test Mode)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

enum class PaymentStage {
    PROCESSING, SUCCESS, FAILED
}
