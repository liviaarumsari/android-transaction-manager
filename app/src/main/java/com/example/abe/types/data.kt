package com.example.abe.types

data class LoginRequest (
    val email: String,
    val password: String
)

data class LoginResponse (
    val token: String
)