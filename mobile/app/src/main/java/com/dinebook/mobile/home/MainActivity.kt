package com.dinebook.mobile.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dinebook.mobile.R
import com.dinebook.mobile.auth.LoginActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val email = intent.getStringExtra("USER_EMAIL") ?: ""

        val tvWelcomeInfo = findViewById<TextView>(R.id.tvWelcomeInfo)
        tvWelcomeInfo.text = getString(R.string.welcome_message)

        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)
        tvUserEmail.text = email

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
