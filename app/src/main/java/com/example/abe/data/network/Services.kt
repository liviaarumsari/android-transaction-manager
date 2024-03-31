package com.example.abe.data.network

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface LoginService {
    @POST("api/auth/login")
    fun login(@Body user: LoginRequest) : Call<LoginResponse>
}

interface ScannerService {
    @Multipart
    @POST("api/bill/upload")
    fun uploadScan(
        @Header("Authorization") authHeader: String,
        @Part file: MultipartBody.Part
    ): Call<ItemsRoot>
}