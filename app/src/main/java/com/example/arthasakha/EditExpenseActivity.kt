package com.example.arthasakha

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.arthasakha.models.Expense
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

class EditExpenseActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var tvCategoryFilter: TextView
    private lateinit var etDescription: EditText
    private lateinit var tvDate: TextView // Date TextView
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var btnBack: ImageView
    private lateinit var sharedPreferences: SharedPreferences

    private var expenseId: String? = null
    private var expense: Expense? = null
    private val gson = Gson()

    private var selectedDate: String = "" // To hold the selected date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_expense)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("expenses_prefs", Context.MODE_PRIVATE)

        // Initialize Views
        etAmount = findViewById(R.id.etAmount)
        tvCategoryFilter = findViewById(R.id.tvCategoryFilter)
        etDescription = findViewById(R.id.etDescription)
        tvDate = findViewById(R.id.tvDate) // Initialize tvDate here
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
        btnBack = findViewById(R.id.btnBack)

        // Get Expense ID
        expenseId = intent.getStringExtra("EXPENSE_ID")

        if (expenseId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Expense!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadExpenseData()

        // Set click listener to show Date Picker
        tvDate.setOnClickListener { showDatePickerDialog() }

        btnSave.setOnClickListener { saveExpenseChanges() }
        btnDelete.setOnClickListener { confirmDeleteExpense() }
        tvCategoryFilter.setOnClickListener { showCategorySelectionDialog() }
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    /**
     * 🔹 Load Existing Expense Data
     */
    private fun loadExpenseData() {
        val expenses = loadExpensesList()
        expense = expenses.find { it.id == expenseId }

        if (expense != null) {
            etAmount.setText(expense!!.amount.toString())
            etDescription.setText(expense!!.description)
            tvCategoryFilter.text = expense!!.category // Set category in TextView
            tvDate.text = expense!!.date // Display current date
            selectedDate = expense!!.date // Save current date
        } else {
            Toast.makeText(this, "Expense not found!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * 🔹 Show Category Selection Dialog
     */
    private fun showCategorySelectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.category_selection_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lvCategoryList = dialogView.findViewById<ListView>(R.id.lvCategoryList)

        // Load categories into ListView
        val categories = resources.getStringArray(R.array.expense_categories)
        val adapter = ArrayAdapter(this, R.layout.category_list_item, R.id.tvCategoryName, categories)
        lvCategoryList.adapter = adapter

        // Handle category selection
        lvCategoryList.setOnItemClickListener { _, _, position, _ ->
            tvCategoryFilter.text = categories[position] // Set selected category
            tvCategoryFilter.setTextColor(Color.BLACK) // Ensure black text
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * 🔹 Show Date Picker Dialog
     */
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // If we already have a selected date, use it to set the calendar's date
        if (selectedDate.isNotEmpty()) {
            try {
                calendar.time = dateFormat.parse(selectedDate) ?: Date()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Format the selected date and update TextView
            selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            tvDate.text = selectedDate
        }, year, month, day)

        datePickerDialog.show()
    }

    /**
     * 🔹 Save Updated Expense Data
     */
    private fun saveExpenseChanges() {
        val updatedAmount = etAmount.text.toString().toDoubleOrNull()
        val updatedCategory = tvCategoryFilter.text.toString()
        val updatedDescription = etDescription.text.toString().trim()

        if (updatedAmount == null || updatedAmount <= 0) {
            etAmount.error = "Enter a valid amount"
            return
        }

        if (updatedCategory.isEmpty() || updatedDescription.isEmpty()) {
            Toast.makeText(this, "Please fill all fields correctly!", Toast.LENGTH_SHORT).show()
            return
        }

        val expenses = loadExpensesList()
        val index = expenses.indexOfFirst { it.id == expenseId }

        if (index != -1) {
            val existingExpense = expenses[index]
            if (existingExpense.amount == updatedAmount &&
                existingExpense.category == updatedCategory &&
                existingExpense.description == updatedDescription &&
                existingExpense.date == selectedDate) {
                Toast.makeText(this, "No changes detected!", Toast.LENGTH_SHORT).show()
                return
            }

            expenses[index] = Expense(
                expenseId!!,
                updatedAmount,
                updatedCategory,
                updatedDescription,
                selectedDate,  // Use the selected date here
                expense!!.title ?: "Untitled Expense"
            )

            saveExpensesList(expenses)
            Toast.makeText(this, "Expense updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error updating expense!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 🔹 Confirm Delete Expense with Alert Dialog
     */
    private fun confirmDeleteExpense() {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Yes") { _, _ -> deleteExpense() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * 🔹 Delete Expense from Shared Preferences
     */
    private fun deleteExpense() {
        val expenses = loadExpensesList()

        if (expenses.removeAll { it.id == expenseId }) {
            saveExpensesList(expenses)
            Toast.makeText(this, "Expense deleted successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error deleting expense!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 🔹 Load Expenses List from Shared Preferences
     */
    private fun loadExpensesList(): MutableList<Expense> {
        val expensesJson = sharedPreferences.getString("expenses_list", "[]")
        val type = object : TypeToken<MutableList<Expense>>() {}.type
        return gson.fromJson(expensesJson, type) ?: mutableListOf()
    }

    /**
     * 🔹 Save Expenses List to Shared Preferences
     */
    private fun saveExpensesList(expenses: MutableList<Expense>) {
        sharedPreferences.edit().putString("expenses_list", gson.toJson(expenses)).apply()
    }
}
