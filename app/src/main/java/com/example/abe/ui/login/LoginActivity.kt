package com.example.abe.ui.login

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.abe.MainActivity
import com.example.abe.connection.ConnectivityObserver
import com.example.abe.connection.NetworkConnectivityObserver
import com.example.abe.data.local.PreferenceDataStoreConstants.TOKEN
import com.example.abe.data.local.PreferenceDataStoreConstants.USER
import com.example.abe.data.local.PreferenceDataStoreHelper
import com.example.abe.data.network.LoginResultCallback
import com.example.abe.data.network.Retrofit
import com.example.abe.databinding.ActivityLoginBinding
import com.example.abe.utils.isConnected
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity(), LoginResultCallback {

    private lateinit var binding: ActivityLoginBinding

    private var email = ""

    private fun attemptLogin(email: String, password: String) {
        val retrofit = Retrofit()
        if (email != "" && password != "") {
            retrofit.login(email, password, this)
        } else {
            Toast.makeText(applicationContext, "Please fill in your email and password", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var connectivityObserver: ConnectivityObserver
    private var networkState: ConnectivityObserver.NetworkState? = null

    private lateinit var preferenceDataStoreHelper: PreferenceDataStoreHelper

    override fun onSuccess(loginResponse: com.example.abe.data.network.LoginResponse) {
        println("Login successful: $loginResponse")

        lifecycleScope.launch {
            preferenceDataStoreHelper.putPreference(TOKEN, loginResponse.token)
            preferenceDataStoreHelper.putPreference(USER, email)
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onFailure(errorMessage: String) {
        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        supportActionBar?.hide()

        val view = binding.root
        setContentView(view)

        connectivityObserver = NetworkConnectivityObserver(applicationContext)
        connectivityObserver.observe().onEach {
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

        preferenceDataStoreHelper = PreferenceDataStoreHelper(applicationContext)

        binding.btnSignIn.setOnClickListener {
            setHelperText(binding.formEmailContainer, binding.emailInput, true)
            setHelperText(binding.formPasswordContainer, binding.passwordInput, false)

            email = binding.emailInput.text.toString()
            val password: String = binding.passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                return@setOnClickListener
            }
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

        emailFocusListener()
        passwordFocusListener()
    }

    private fun emailFocusListener() {
        binding.emailInput.setOnFocusChangeListener { _, focused ->
            if (!focused) setHelperText(binding.formEmailContainer, binding.emailInput, true)
        }
    }

    private fun passwordFocusListener() {
        binding.passwordInput.setOnFocusChangeListener { _, focused ->
            if (!focused) setHelperText(binding.formPasswordContainer, binding.passwordInput, false)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setHelperText(container: TextInputLayout, editText: EditText, isEmail: Boolean) {
        if (editText.text.toString().isEmpty()) {
            container.helperText = "This field is required"
        } else if (isEmail && !isValidEmail(editText.text.toString())) {
            container.helperText = "Invalid email address"
        } else {
            container.helperText = null
        }
    }
}
