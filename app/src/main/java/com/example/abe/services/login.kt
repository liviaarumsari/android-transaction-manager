package com.example.abe.services

import com.example.abe.types.LoginRequest
import com.example.abe.types.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("api/auth/login")
    fun login(@Body user: LoginRequest) : Call<LoginResponse>
}