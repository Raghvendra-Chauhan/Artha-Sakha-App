package com.example.arthasakha

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // 🔹 UI Elements
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnManageUsers = findViewById<Button>(R.id.btnManageUsers)
        val btnViewReports = findViewById<Button>(R.id.btnViewReports)

        // ✅ Get Admin Email from Intent (if available)
        val adminEmail = intent.getStringExtra("ADMIN_EMAIL") ?: "Admin"
        tvWelcome.text = "Welcome, $adminEmail"

        // ✅ Logout button action
        btnLogout?.setOnClickListener {
            logoutAdmin()
        }

        // ✅ Navigate to Manage Users Page
        btnManageUsers?.setOnClickListener {
            startActivity(Intent(this, ManageUsersActivity::class.java))
        }

        // ✅ Navigate to View Reports Page
        btnViewReports?.setOnClickListener {
            startActivity(Intent(this, ViewReportsActivity::class.java))
        }
    }

    /**
     * ✅ Logs out the admin and redirects to Login Page
     * - Clears Firebase session
     * - Clears activity stack to prevent back navigation
     */
    private fun logoutAdmin() {
        FirebaseAuth.getInstance().signOut() // Ensures Firebase logout
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
