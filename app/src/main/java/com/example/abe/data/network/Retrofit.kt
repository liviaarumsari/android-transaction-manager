package com.example.abe.data.network

import android.content.Context
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

interface LoginResultCallback {
    fun onSuccess(loginResponse: LoginResponse)
    fun onFailure(errorMessage: String)
}

interface UploadResultCallback {
    fun onSuccess(uploadResponse: ItemsRoot)
    fun onFailure(errorMessage: String)
}

class Retrofit {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://pbd-backend-2024.vercel.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun login(email: String, password: String, callback: LoginResultCallback) {
        val loginService = retrofit.create(LoginService::class.java)
        val call: Call<LoginResponse> = loginService.login(LoginRequest(email, password))

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback.onSuccess(it)
                    }
                } else {
                    callback.onFailure("Login failed")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                callback.onFailure("Failed to send request")
            }
        })
    }

    fun upload(context: Context, file: File, callback: UploadResultCallback) {
        val scannerService = retrofit.create(ScannerService::class.java)
        val sharedPreferences = context.getSharedPreferences("YourPreferenceName", Context.MODE_PRIVATE)
        val authHeader = "Bearer " + sharedPreferences.getString("login_token", "")

        // create RequestBody instance from file
        val requestFile = RequestBody.create(
            MediaType.parse("multipart/form-data"),
            file
        )

        // MultipartBody.Part is used to send also the actual file name
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val call: Call<ItemsRoot> = scannerService.uploadScan(authHeader, body)
    }

}