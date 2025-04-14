package com.example.arthasakha

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private var isDarkTheme = false // toggle flag for demo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<Button>(R.id.btnSetBudget).setOnClickListener {
            showBudgetDialog()
        }

        findViewById<Button>(R.id.btnChangeTheme).setOnClickListener {
            toggleTheme()
        }

        findViewById<Button>(R.id.btnChangeCurrency).setOnClickListener {
            showCurrencyDialog()
        }

        findViewById<Button>(R.id.btnUpdateProfile).setOnClickListener {
            updateProfile()
        }

        findViewById<Button>(R.id.btnNotifications).setOnClickListener {
            showNotificationSettings()
        }

        findViewById<Button>(R.id.btnExportData).setOnClickListener {
            exportData()
        }

        findViewById<Button>(R.id.btnImportData).setOnClickListener {
            importData()
        }

        findViewById<Button>(R.id.btnResetData).setOnClickListener {
            resetData()
        }

        findViewById<Button>(R.id.btnFeedback).setOnClickListener {
            sendFeedback()
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }
    }

    private fun showBudgetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set Budget")

        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val budgetText = input.text.toString()
            val budget = budgetText.toIntOrNull()

            if (budget != null && budget > 0) {
                SharedPreferencesHelper.saveBudget(this, budget)
                Toast.makeText(this, "Budget Set: ₹$budget", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun showCurrencyDialog() {
        val currencies = arrayOf("INR ₹", "USD $", "EUR €", "JPY ¥")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Currency")
        builder.setItems(currencies) { _, which ->
            val selected = currencies[which]
            Toast.makeText(this, "Currency set to $selected", Toast.LENGTH_SHORT).show()
            // Save selected currency in SharedPreferences if needed
        }
        builder.show()
    }

    private fun updateProfile() {
        Toast.makeText(this, "Redirecting to Update Profile...", Toast.LENGTH_SHORT).show()
        // Launch update profile activity or dialog
    }

    private fun showNotificationSettings() {
        Toast.makeText(this, "Notification settings coming soon", Toast.LENGTH_SHORT).show()
        // Open a notification settings screen or switch toggle
    }

    private fun exportData() {
        Toast.makeText(this, "Exporting data...", Toast.LENGTH_SHORT).show()
        // Export CSV logic here
    }

    private fun importData() {
        Toast.makeText(this, "Importing data...", Toast.LENGTH_SHORT).show()
        // Import CSV logic here
    }

    private fun resetData() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset All Data")
        builder.setMessage("Are you sure you want to delete all data?")
        builder.setPositiveButton("Yes") { _, _ ->
            SharedPreferencesHelper.clearAll(this)
            Toast.makeText(this, "All data reset", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun sendFeedback() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@arthasakha.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback for Artha-Sakha")
            putExtra(Intent.EXTRA_TEXT, "Write your feedback here...")
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "No email client installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        // Clear session and redirect to login
    }
}
