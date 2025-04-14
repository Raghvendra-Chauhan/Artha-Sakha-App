package com.example.arthasakha

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import android.app.Dialog


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("users")

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val ivTogglePassword = findViewById<ImageView>(R.id.ivTogglePassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvNewUser = findViewById<TextView>(R.id.tvNewUser)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvSkip = findViewById<TextView>(R.id.tvSkip)

        var isPasswordVisible = false

        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(etPassword.text.length)
            ivTogglePassword.setImageResource(
                if (isPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Email cannot be empty"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Password cannot be empty"
                return@setOnClickListener
            }

            updateUI(isLoading = true, btnLogin, progressBar, tvSkip)

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        if (user != null && !user.isEmailVerified) {
                            AlertDialog.Builder(this)
                                .setTitle("Email Verification Required")
                                .setMessage("Please verify your email before logging in.")
                                .setPositiveButton("OK") { _, _ -> auth.signOut() }
                                .show()
                            updateUI(isLoading = false, btnLogin, progressBar, tvSkip)
                            return@addOnCompleteListener
                        }

                        checkUserRoleAndNavigate(user, btnLogin, progressBar, tvSkip)
                    } else {
                        val errorMessage = task.exception?.localizedMessage ?: "Login failed. Try again."
                        showToast(errorMessage)
                        Log.e("LoginActivity", "Login Error: ${task.exception}")
                        updateUI(isLoading = false, btnLogin, progressBar, tvSkip)
                    }
                }
        }

        tvNewUser.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        tvSkip.visibility = View.VISIBLE
        tvSkip.setOnClickListener {
            Log.d("LoginActivity", "Skip button clicked")
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("GUEST_MODE", true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

    }

    private fun checkUserRoleAndNavigate(user: FirebaseUser?, btnLogin: Button, progressBar: ProgressBar, tvSkip: TextView) {
        if (user == null) {
            Log.e("LoginActivity", "User is null, redirecting to login.")
            showToast("Authentication failed. Try again.")
            updateUI(isLoading = false, btnLogin, progressBar, tvSkip)
            return
        }

        database.child(user.uid).child("role").get()
            .addOnSuccessListener { snapshot ->
                val role = snapshot.value?.toString() ?: "user"

                val intent = when (role) {
                    "admin" -> Intent(this, AdminActivity::class.java)
                    else -> Intent(this, MainActivity::class.java)
                }

                Log.d("LoginActivity", "User role: $role - Navigating to appropriate screen")
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Log.e("LoginActivity", "Error fetching user role: ${it.message}")
                showToast("Error fetching user role.")
                updateUI(isLoading = false, btnLogin, progressBar, tvSkip)
            }
    }

    private fun updateUI(isLoading: Boolean, btnLogin: Button, progressBar: ProgressBar, tvSkip: TextView) {
        btnLogin.isEnabled = !isLoading
        tvSkip.visibility = if (isLoading) View.GONE else View.VISIBLE
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showForgotPasswordDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_forgot_password)
        dialog.setCancelable(false)  // Prevent accidental dismissal
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etResetEmail = dialog.findViewById<EditText>(R.id.etResetEmail)
        val btnSend = dialog.findViewById<Button>(R.id.btnSendEmail)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)


        btnSend.setOnClickListener {
            val email = etResetEmail.text.toString().trim()
            if (email.isEmpty()) {
                etResetEmail.error = "Enter your email"
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        showToast("Password reset email sent")
                        dialog.dismiss()  // Close dialog after success
                    }
                    .addOnFailureListener {
                        showToast("Error: ${it.message}")
                    }
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()  // Close dialog on cancel
        }

        dialog.show()
    }

}
