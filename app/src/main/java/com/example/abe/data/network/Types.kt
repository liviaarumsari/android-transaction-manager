package com.example.abe.data.network

data class LoginRequest (
    val email: String,
    val password: String
)

data class LoginResponse (
    val token: String
)

data class TransactionItem(
    val name: String,
    val qty: Int,
    val price: Double
)

data class ItemsContainer(
    val items: List<TransactionItem>
)

data class ItemsRoot(
    val items: ItemsContainer
)

data class CheckAuthResponse (
    val nim: String,
    val iat: String,
    val exp: String
)
