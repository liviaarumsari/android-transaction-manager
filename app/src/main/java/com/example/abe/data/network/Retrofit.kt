package com.example.abe.data.network

import android.content.Context
import android.util.Log
import com.example.abe.R
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.net.URLConnection


interface LoginResultCallback {
    fun onSuccess(loginResponse: LoginResponse)
    fun onFailure(errorMessage: String)
}

interface UploadResultCallback {
    fun onSuccess(uploadResponse: ItemsRoot)
    fun onFailure(errorMessage: String)
}

interface CheckAuthResultCallback {
    fun onFailure()
}

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

    private val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://pbd-backend-2024.vercel.app/")
        .client(client)
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
                    callback.onFailure("Login failed, incorrect email or password")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                callback.onFailure("Failed to send login request")
            }
        })
    }

    fun upload(context: Context, file: File, callback: UploadResultCallback) {
        val scannerService = retrofit.create(ScannerService::class.java)
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val authHeader = "Bearer " + sharedPreferences.getString("login_token", "")

        // Determine the MIME type of the file
        val mimeType = URLConnection.guessContentTypeFromName(file.name)
        if (mimeType == null) {
            Log.e("ABE-PHO", "Could not determine MIME type of file")
            return
        }

        // create RequestBody instance from file
        val requestFile = file
            .asRequestBody(mimeType.toMediaTypeOrNull())


        // MultipartBody.Part is used to send also the actual file name
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val call: Call<ItemsRoot> = scannerService.uploadScan(authHeader, body)

        call.enqueue(object: Callback<ItemsRoot> {
            override fun onResponse(call: Call<ItemsRoot>, response: Response<ItemsRoot>) {
//                Log.d("ABE-PHO", "response: " + Gson().toJson(response.body()))
//                Log.d("ABE-PHO", "error: " + Gson().toJson(response.errorBody()))
//                Log.d("ABE-PHO", "code: " + response.code())
//                Log.d("ABE-PHO", "headers: " + response.headers())
//                Log.d("ABE-PHO", "message: " + response.message())

                if (response.isSuccessful) {
                    response.body()?.let {
                        callback.onSuccess(it)
                    }
                } else {
                    callback.onFailure("Upload failed")
                }
            }

            override fun onFailure(call: Call<ItemsRoot>, t: Throwable) {
                Log.d("ABE-PHO", "Failed to send request: " + t.message)
                callback.onFailure("Failed to send photo")
            }
        })
    }


    fun checkAuth(token: String, callback: CheckAuthResultCallback) {
        val checkAuthService = retrofit.create(CheckAuthService::class.java)
        val call: Call<CheckAuthResponse> = checkAuthService.checkAuth("Bearer $token")

        call.enqueue(object : Callback<CheckAuthResponse> {
            override fun onResponse(call: Call<CheckAuthResponse>, response: Response<CheckAuthResponse>) {
                if (!response.isSuccessful) {
                    callback.onFailure()
                }
            }

            override fun onFailure(call: Call<CheckAuthResponse>, t: Throwable) {
                callback.onFailure()
            }
        })
    }
}