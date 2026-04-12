package com.dinebook.mobile.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dinebook.mobile.R
import com.dinebook.mobile.api.ApiClient
import com.dinebook.mobile.models.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: Button
    private lateinit var tabLogin: TextView
    private lateinit var tabRegister: TextView
    private lateinit var tvMessage: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        tabLogin = findViewById(R.id.tabLogin)
        tabRegister = findViewById(R.id.tabRegister)
        tvMessage = findViewById(R.id.tvMessage)
        progressBar = findViewById(R.id.progressBar)

        // Tab: Login is active, Register navigates away
        tabRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }

        btnLogin.setOnClickListener {
            performLogin()
        }

        btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "Google Login coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showMessage(getString(R.string.error_empty_fields), isError = true)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage(getString(R.string.error_invalid_email), isError = true)
            return
        }

        showLoading(true)
        hideMessage()

        lifecycleScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = withContext(Dispatchers.IO) {
                    ApiClient.authService.login(request)
                }

                Log.d(TAG, "Login response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()
                    Log.d(TAG, "Login success - email: ${authResponse?.email}, token: ${authResponse?.accessToken?.take(20)}...")
                    showMessage(getString(R.string.success_login), isError = false)

                    // Short delay then redirect (like the web)
                    kotlinx.coroutines.delay(1500)

                    val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                        putExtra("USER_EMAIL", authResponse?.email ?: email)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    // Read the error body for detailed info
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(TAG, "Login failed - code: ${response.code()}, body: $errorBody")
                    showMessage("Login failed (${response.code()}): $errorBody", isError = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login exception", e)
                showMessage("Error: ${e.message}", isError = true)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showMessage(text: String, isError: Boolean) {
        tvMessage.visibility = View.VISIBLE
        tvMessage.text = text
        if (isError) {
            tvMessage.setBackgroundColor(ContextCompat.getColor(this, R.color.error_bg))
            tvMessage.setTextColor(ContextCompat.getColor(this, R.color.error_text))
        } else {
            tvMessage.setBackgroundColor(ContextCompat.getColor(this, R.color.success_bg))
            tvMessage.setTextColor(ContextCompat.getColor(this, R.color.success_text))
        }
    }

    private fun hideMessage() {
        tvMessage.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
        btnGoogleLogin.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
        tabRegister.isEnabled = !isLoading

        btnLogin.text = if (isLoading) getString(R.string.action_processing) else getString(R.string.action_login)
    }
}
