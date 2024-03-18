package com.example.abe.data.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CallBack<T> : Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful) {
            // Handle successful response
            val data = response.body()
            // Process the data here
            if (data is LoginResponse) {
                // Handle LoginResponse
                println("Login successful $data")
            }
        } else {
            // Handle error response
            // Maybe use response.errorBody() to get error details
            println("Login failed")
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        // Handle failure
        println("Failed to send request")
    }
}

class Retrofit {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://pbd-backend-2024.vercel.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun login(email: String, password: String) {
        val loginService = retrofit.create(LoginService::class.java)

        val call: Call<LoginResponse> = loginService.login(
            LoginRequest(email, password)
        )

        call.enqueue(CallBack())
    }
}