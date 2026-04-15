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
import com.dinebook.mobile.models.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RegisterActivity"
    }

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnGoogleLogin: Button
    private lateinit var tabLogin: TextView
    private lateinit var tabRegister: TextView
    private lateinit var tvMessage: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        tabLogin = findViewById(R.id.tabLogin)
        tabRegister = findViewById(R.id.tabRegister)
        tvMessage = findViewById(R.id.tvMessage)
        progressBar = findViewById(R.id.progressBar)

        // Tab: Register is active, Login navigates away
        tabLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }

        btnRegister.setOnClickListener {
            performRegistration()
        }

        btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "Google Login coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performRegistration() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage(getString(R.string.error_empty_fields), isError = true)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage(getString(R.string.error_invalid_email), isError = true)
            return
        }

        if (password.length < 6) {
            showMessage(getString(R.string.error_invalid_password), isError = true)
            return
        }

        if (password != confirmPassword) {
            showMessage(getString(R.string.error_passwords_mismatch), isError = true)
            return
        }

        showLoading(true)
        hideMessage()

        lifecycleScope.launch {
            try {
                val request = RegisterRequest(
                    email = email,
                    password = password,
                    fullName = name
                )

                val response = withContext(Dispatchers.IO) {
                    ApiClient.authService.register(request)
                }

                Log.d(TAG, "Register response code: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d(TAG, "Registration successful for: $email")
                    showMessage(getString(R.string.success_register), isError = false)

                    // Short delay then redirect (like the web)
                    kotlinx.coroutines.delay(1500)

                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(TAG, "Register failed - code: ${response.code()}, body: $errorBody")
                    showMessage("Registration failed (${response.code()}): $errorBody", isError = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Register exception", e)
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
        btnRegister.isEnabled = !isLoading
        btnGoogleLogin.isEnabled = !isLoading
        etName.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
        etConfirmPassword.isEnabled = !isLoading
        tabLogin.isEnabled = !isLoading

        btnRegister.text = if (isLoading) getString(R.string.action_processing) else getString(R.string.action_register)
    }
}
