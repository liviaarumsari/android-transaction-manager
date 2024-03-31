package com.example.abe.data.network

data class LoginRequest (
    val email: String,
    val password: String
)

data class LoginResponse (
    val token: String
)

data class Item(
    val name: String,
    val qty: Int,
    val price: Double
)

data class ItemsContainer(
    val items: List<Item>
)

data class ItemsRoot(
    val items: ItemsContainer
)
