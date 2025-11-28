package com.example.apphilos.repository

import android.util.Log
import com.example.apphilos.model.Order
import com.example.apphilos.model.OrderStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.Dispatchers

class OrderRepository {
    // CONCEPTO: Semaphore - Limita a 3 cocinas trabajando simultáneamente
    private val kitchenSemaphore = Semaphore(3)

    // CONCEPTO: Flow - Emite el progreso de cocción
    fun processOrder(order: Order): Flow<Order> = flow {
        // IMPORTANTE: Ya NO usamos withContext aquí
        Log.d("OrderRepository", "processOrder: Iniciando #${order.id}")

        // CONCEPTO: Semaphore.acquire() - Adquiere permiso de cocina
        Log.d("OrderRepository", "processOrder: Adquiriendo semáforo #${order.id}")
        kitchenSemaphore.acquire()

        try {
            Log.d("OrderRepository", "processOrder: Cocinando #${order.id}")

            // Cambiar a estado COOKING
            emit(order.copy(status = OrderStatus.COOKING, progress = 0f))

            // Simular cocción con progreso
            val steps = 10
            val stepTime = order.cookingTime / steps

            for (i in 1..steps) {
                delay(stepTime)
                val progress = i / steps.toFloat()
                emit(order.copy(
                    status = OrderStatus.COOKING,
                    progress = progress
                ))
                Log.d("OrderRepository", "processOrder: #${order.id} - ${(progress * 100).toInt()}%")
            }

            // Pedido listo
            emit(order.copy(status = OrderStatus.READY, progress = 1f))
            Log.d("OrderRepository", "processOrder: Completado #${order.id}")

        } finally {
            // CONCEPTO: Semaphore.release() - Libera cocina
            kitchenSemaphore.release()
            Log.d("OrderRepository", "processOrder: Liberando semáforo #${order.id}")
        }
    }.flowOn(Dispatchers.Default) // ← ESTO ES LA CLAVE: mueve TODO el Flow a Dispatchers.Default
}