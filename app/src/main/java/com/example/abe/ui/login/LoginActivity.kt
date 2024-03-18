package com.example.abe.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.abe.MainActivity
import com.example.abe.data.network.Retrofit
import com.example.abe.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        binding.btnSignIn.setOnClickListener {
            val email: String = binding.emailInput.text.toString()
            val password: String = binding.passwordInput.text.toString()

            val connection = Retrofit()
            connection.login(email, password)

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
