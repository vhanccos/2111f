package com.example.apphilos.model

data class Order(
    val id: Int,
    val name: String,
    val cookingTime: Long, // Milisegundos
    val status: OrderStatus = OrderStatus.PENDING,
    val progress: Float = 0f
)