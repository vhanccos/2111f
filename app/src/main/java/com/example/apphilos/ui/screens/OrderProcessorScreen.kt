package com.example.apphilos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apphilos.ui.components.OrderCard
import com.example.apphilos.ui.components.ControlPanel
import com.example.apphilos.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderProcessorScreen(
    viewModel: OrderViewModel = viewModel()
) {
    // CONCEPTO: collectAsState - Observa StateFlow en Compose
    val orders by viewModel.orders.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // CONCEPTO: LaunchedEffect - Efecto que se ejecuta cuando cambia la key
    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🍕 Procesador de Pedidos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Panel de Control
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ControlPanel(
                    onAddOrder = { viewModel.addOrder() },
                    onProcessAll = { viewModel.processAllPendingOrders() },
                    onClearOrders = { viewModel.clearOrders() },
                    onBackup = { viewModel.scheduleBackup() }
                )
            }

            // Estadísticas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "📊 Estadísticas",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text("Total pedidos: ${orders.size}")
                        Text("Pendientes: ${orders.count { it.status == com.example.apphilos.model.OrderStatus.PENDING }}")
                        Text("Cocinando: ${orders.count { it.status == com.example.apphilos.model.OrderStatus.COOKING }}")
                        Text("Listos: ${orders.count { it.status == com.example.apphilos.model.OrderStatus.READY }}")
                    }
                }
            }

            // Lista de pedidos
            items(orders.reversed(), key = { it.id }) { order ->
                OrderCard(
                    order = order,
                    onProcess = { viewModel.processOrder(order) }
                )
            }

            // Espaciado final
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}