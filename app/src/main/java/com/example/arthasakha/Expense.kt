package com.example.arthasakha.models

data class Expense(
    val id: String,
    val amount: Double,
    val category: String,
    val description: String,
    val date: String,
    val title: String? = null,
    val isUPITransaction: Boolean = false,  // New field to identify UPI transactions
    val upiTransactionId: String? = null
)
