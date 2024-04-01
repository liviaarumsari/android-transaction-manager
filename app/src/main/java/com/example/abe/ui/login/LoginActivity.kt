package com.example.abe.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.abe.MainActivity
import com.example.abe.R
import com.example.abe.connection.ConnectivityObserver
import com.example.abe.connection.NetworkConnectivityObserver
import com.example.abe.data.network.LoginResultCallback
import com.example.abe.data.network.Retrofit
import com.example.abe.databinding.ActivityLoginBinding
import com.example.abe.utils.isConnected
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginActivity : AppCompatActivity(), LoginResultCallback {

    private lateinit var binding: ActivityLoginBinding

    private var email = ""

    private fun attemptLogin(email: String, password: String) {
        val retrofit = Retrofit()
        retrofit.login(email, password, this)
    }

    private lateinit var connectivityObserver: ConnectivityObserver
    private var networkState: ConnectivityObserver.NetworkState? = null

    override fun onSuccess(loginResponse: com.example.abe.data.network.LoginResponse) {
        // Handle successful login
        println("Login successful: $loginResponse")

        val sharedPref =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
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

        connectivityObserver = NetworkConnectivityObserver(applicationContext)
        connectivityObserver.observe().onEach {
            Log.v("abecekut", "Status is $it")
            networkState = it
            if (it == ConnectivityObserver.NetworkState.UNAVAILABLE || it == ConnectivityObserver.NetworkState.LOST) {
                runOnUiThread {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this@LoginActivity)
                    builder
                        .setMessage("We are having trouble connecting you to the internet")
                        .setTitle("No Connection")
                        .setPositiveButton("OK") { _, _ -> }

                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
        }.launchIn(lifecycleScope)

        binding.btnSignIn.setOnClickListener {
            email = binding.emailInput.text.toString()
            val password: String = binding.passwordInput.text.toString()

            if (!isConnected(networkState)) {
                binding.loginLayout.visibility = View.GONE
                binding.noNetworkLayout.visibility = View.VISIBLE
            } else {
                attemptLogin(email, password)
            }
        }

        binding.btnTryAgain.setOnClickListener {
            binding.noNetworkLayout.visibility = View.GONE
            binding.loginLayout.visibility = View.VISIBLE
        }
    }
}
