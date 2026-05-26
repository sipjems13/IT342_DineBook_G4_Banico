package com.dinebook.mobile.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dinebook.mobile.R
import com.dinebook.mobile.home.MainActivity
import com.dinebook.mobile.shared.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
        private const val SUPABASE_URL = "https://zruzhkwunykhyhuyytei.supabase.co"
        private const val MOBILE_REDIRECT = "dinebook://auth-callback"
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

        tabRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }

        btnLogin.setOnClickListener { performLogin() }

        btnGoogleLogin.setOnClickListener {
            startGoogleLogin()
        }

        handleOAuthCallback(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthCallback(intent)
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
                    showMessage(getString(R.string.success_login), isError = false)
                    kotlinx.coroutines.delay(1500)
                    val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                        putExtra("USER_EMAIL", authResponse?.email ?: email)
                        putExtra("ACCESS_TOKEN", authResponse?.accessToken)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
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

    private fun hideMessage() { tvMessage.visibility = View.GONE }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
        btnGoogleLogin.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
        tabRegister.isEnabled = !isLoading
        btnLogin.text = if (isLoading) getString(R.string.action_processing) else getString(R.string.action_login)
    }

    private fun startGoogleLogin() {
        val redirect = Uri.encode(MOBILE_REDIRECT)
        val url = "$SUPABASE_URL/auth/v1/authorize?provider=google&redirect_to=$redirect"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun handleOAuthCallback(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "dinebook" || uri.host != "auth-callback") return

        val values = parseCallbackValues(uri)
        val token = values["access_token"]
        if (token.isNullOrBlank()) {
            showMessage("Google Login failed. Please try again.", isError = true)
            return
        }

        val email = emailFromJwt(token) ?: "Google account"
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_EMAIL", email)
            putExtra("ACCESS_TOKEN", token)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(mainIntent)
        finish()
    }

    private fun parseCallbackValues(uri: Uri): Map<String, String> {
        val raw = uri.fragment ?: uri.query ?: return emptyMap()
        return raw.split("&")
            .mapNotNull {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) parts[0] to Uri.decode(parts[1]) else null
            }
            .toMap()
    }

    private fun emailFromJwt(token: String): String? {
        return try {
            val payload = token.split(".").getOrNull(1) ?: return null
            val decoded = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
            JSONObject(decoded).optString("email").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
