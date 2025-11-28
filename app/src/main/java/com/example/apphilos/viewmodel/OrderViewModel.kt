package com.example.apphilos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apphilos.model.Order
import com.example.apphilos.model.OrderStatus
import com.example.apphilos.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.async
import java.util.concurrent.atomic.AtomicInteger

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val orderCounter = AtomicInteger(0)
    private val ordersMutex = Mutex()

    private val _events = Channel<String>()
    val events = _events.receiveAsFlow()

    private val orderNames = listOf(
        "Pizza Margarita", "Hamburguesa", "Sushi Roll",
        "Tacos", "Pasta Carbonara", "Ensalada César"
    )

    fun addOrder() {
        viewModelScope.launch {
            try {
                Log.d("OrderViewModel", "addOrder: Iniciando")
                val orderId = orderCounter.incrementAndGet()
                val newOrder = Order(
                    id = orderId,
                    name = orderNames.random(),
                    cookingTime = (2000L..6000L).random()
                )

                ordersMutex.withLock {
                    _orders.value = _orders.value + newOrder
                }

                _events.send("Pedido #$orderId agregado")
                Log.d("OrderViewModel", "addOrder: Completado #$orderId")
            } catch (e: Exception) {
                Log.e("OrderViewModel", "addOrder: ERROR", e)
            }
        }
    }

    fun processOrder(order: Order) {
        viewModelScope.launch {
            try {
                Log.d("OrderViewModel", "processOrder: Iniciando #${order.id}")

                repository.processOrder(order).collect { updatedOrder ->
                    Log.d("OrderViewModel", "processOrder: Actualizando #${updatedOrder.id} - ${updatedOrder.status}")
                    updateOrder(updatedOrder)
                }

                Log.d("OrderViewModel", "processOrder: Completado #${order.id}")
            } catch (e: Exception) {
                Log.e("OrderViewModel", "processOrder: ERROR #${order.id}", e)
            }
        }
    }

    fun processAllPendingOrders() {
        viewModelScope.launch {
            try {
                Log.d("OrderViewModel", "processAllPendingOrders: Iniciando")
                val pending = _orders.value.filter { it.status == OrderStatus.PENDING }
                Log.d("OrderViewModel", "processAllPendingOrders: ${pending.size} pedidos pendientes")

                val jobs = pending.map { order ->
                    async {
                        repository.processOrder(order).collect { updatedOrder ->
                            updateOrder(updatedOrder)
                        }
                    }
                }

                jobs.forEach { it.await() }

                _events.send("Todos los pedidos procesados")
                Log.d("OrderViewModel", "processAllPendingOrders: Completado")
            } catch (e: Exception) {
                Log.e("OrderViewModel", "processAllPendingOrders: ERROR", e)
            }
        }
    }

    private suspend fun updateOrder(order: Order) {
        try {
            ordersMutex.withLock {
                _orders.value = _orders.value.map {
                    if (it.id == order.id) order else it
                }
            }
        } catch (e: Exception) {
            Log.e("OrderViewModel", "updateOrder: ERROR #${order.id}", e)
        }
    }

    fun clearOrders() {
        viewModelScope.launch {
            try {
                Log.d("OrderViewModel", "clearOrders: Iniciando")
                ordersMutex.withLock {
                    _orders.value = emptyList()
                    orderCounter.set(0)
                }
                Log.d("OrderViewModel", "clearOrders: Completado")
            } catch (e: Exception) {
                Log.e("OrderViewModel", "clearOrders: ERROR", e)
            }
        }
    }
}