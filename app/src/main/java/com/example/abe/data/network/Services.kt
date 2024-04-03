package com.example.abe.data.network

import com.example.abe.data.network.LoginRequest
import com.example.abe.data.network.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LoginService {
    @POST("api/auth/login")
    fun login(@Body user: LoginRequest) : Call<LoginResponse>
}

interface CheckAuthService {
    @POST("api/auth/token")
    fun checkAuth(@Header("Authorization") token: String) : Call<CheckAuthResponse>
}