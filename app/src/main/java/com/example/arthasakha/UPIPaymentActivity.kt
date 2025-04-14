package com.example.arthasakha

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arthasakha.models.Expense
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UPIPaymentActivity : AppCompatActivity() {

    companion object {
        private const val UPI_PAYMENT_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val upiUri = intent.getStringExtra("upi_uri")

        if (upiUri.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid UPI request", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUri))
        startActivityForResult(intent, UPI_PAYMENT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UPI_PAYMENT_REQUEST_CODE) {
            data?.getStringExtra("response")?.let { response ->
                val details = parseUPIResponse(response)
                if (details["status"] == "success") {
                    saveUPITransaction(details)
                    Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Payment Failed!", Toast.LENGTH_SHORT).show()
                }
            }
            finish()
        }
    }

    private fun parseUPIResponse(response: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        response.split("&").forEach {
            val pair = it.split("=")
            if (pair.size == 2) {
                map[pair[0]] = pair[1]
            }
        }
        return map
    }

    private fun saveUPITransaction(details: Map<String, String>) {
        val sharedPreferences = getSharedPreferences("expenses_prefs", Context.MODE_PRIVATE)
        val gson = Gson()

        val newExpense = Expense(
            id = System.currentTimeMillis().toString(),  // Convert Long to String
            title = "UPI Payment",
            amount = details["amount"]?.toDoubleOrNull() ?: 0.0,
            category = "UPI",
            description = details["description"] ?: "UPI Transaction", // Fix: Added description
            date = System.currentTimeMillis().toString(),
            isUPITransaction = true,
            upiTransactionId = details["txnId"] ?: "N/A"
        )

        val expensesJson = sharedPreferences.getString("expenses_list", "[]")
        val type = object : TypeToken<List<Expense>>() {}.type
        val expenses: MutableList<Expense> = gson.fromJson(expensesJson, type) ?: mutableListOf()

        expenses.add(0, newExpense)

        sharedPreferences.edit().putString("expenses_list", gson.toJson(expenses)).apply()
    }
}
