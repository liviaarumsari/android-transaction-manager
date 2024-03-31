package com.example.abe.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.abe.MainActivity
import com.example.abe.R
import com.example.abe.data.network.LoginResultCallback
import com.example.abe.data.network.Retrofit
import com.example.abe.databinding.ActivityLoginBinding
import com.example.abe.data.network.LoginResponse

class LoginActivity : AppCompatActivity(), LoginResultCallback {

    private lateinit var binding: ActivityLoginBinding

    private var email = ""

    private fun attemptLogin(email: String, password: String) {
        val retrofit = Retrofit()
        retrofit.login(email, password, this)
    }

    override fun onSuccess(loginResponse: com.example.abe.data.network.LoginResponse) {
        // Handle successful login
        println("Login successful: $loginResponse")

        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("login_token", loginResponse.token)
            putString("user", email)
            apply()
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onFailure(errorMessage: String) {
        // Handle login failure
        println(errorMessage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        supportActionBar?.hide()

        val view = binding.root
        setContentView(view)

        binding.btnSignIn.setOnClickListener {
            email = binding.emailInput.text.toString()
            val password: String = binding.passwordInput.text.toString()

            attemptLogin(email, password)
        }
    }
}
