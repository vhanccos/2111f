package com.example.apphilos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apphilos.model.Order
import com.example.apphilos.model.OrderStatus

@Composable
fun OrderCard(
    order: Order,
    onProcess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (order.status) {
                OrderStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                OrderStatus.COOKING -> MaterialTheme.colorScheme.primaryContainer
                OrderStatus.READY -> MaterialTheme.colorScheme.tertiaryContainer
                OrderStatus.DELIVERED -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pedido #${order.id}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = order.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = when (order.status) {
                        OrderStatus.PENDING -> "⏳ Pendiente"
                        OrderStatus.COOKING -> "🔥 Cocinando"
                        OrderStatus.READY -> "✅ Listo"
                        OrderStatus.DELIVERED -> "📦 Entregado"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (order.status == OrderStatus.COOKING) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { order.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${(order.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (order.status == OrderStatus.PENDING) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onProcess,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cocinar")
                }
            }
        }
    }
}