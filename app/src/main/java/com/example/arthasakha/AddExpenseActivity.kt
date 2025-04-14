package com.example.arthasakha

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.arthasakha.models.Expense
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.app.Dialog


class AddExpenseActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvCategoryFilter: TextView
    private lateinit var btnSaveExpense: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var calendarView: CalendarView
    private val gson = Gson()
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // ✅ Initialize Views
        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        tvCategoryFilter = findViewById(R.id.tvCategoryFilter)
        btnSaveExpense = findViewById(R.id.btnSaveExpense)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)
        calendarView = findViewById(R.id.calendarView)

        // ✅ Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("expenses_prefs", Context.MODE_PRIVATE)

        // ✅ Handle Back Button Click
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ✅ Handle Category Selection Dialog
        tvCategoryFilter.setOnClickListener {
            showCategoryDialog()
        }

        // ✅ Handle Save Expense Button Click
        btnSaveExpense.setOnClickListener {
            saveExpense()
        }

        // ✅ Handle Calendar Date Selection
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedCalendar.time)
        }
    }

    private fun showCategoryDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.category_selection_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lvCategoryList = dialogView.findViewById<ListView>(R.id.lvCategoryList)
        val categories = resources.getStringArray(R.array.expense_categories)
        val adapter = ArrayAdapter(this, R.layout.category_list_item, R.id.tvCategoryName, categories)
        lvCategoryList.adapter = adapter

        // Handle category selection
        lvCategoryList.setOnItemClickListener { _, _, position, _ ->
            tvCategoryFilter.text = categories[position]
            tvCategoryFilter.setTextColor(Color.BLACK)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveExpense() {
        val amountText = etAmount.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val category = tvCategoryFilter.text.toString()

        // If date is not selected, default to today
        if (selectedDate.isEmpty()) {
            selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }

        // ✅ Validate Input
        if (amountText.isEmpty()) {
            etAmount.error = "Enter amount"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            etAmount.error = "Enter a valid amount"
            return
        }

        if (description.isEmpty()) {
            etDescription.error = "Enter a description"
            return
        }

        if (category == "Select Category") {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ Create Expense Object
        val expenseId = UUID.randomUUID().toString()
        val expense = Expense(expenseId, amount, category, description, selectedDate)

        // ✅ Show Progress Bar
        progressBar.visibility = View.VISIBLE

        // ✅ Save Expense
        val expenses = loadExpensesList()
        expenses.add(expense)
        saveExpensesList(expenses)

        // ✅ Hide Progress Bar
        progressBar.visibility = View.GONE
        Toast.makeText(this, "Expense Added Successfully!", Toast.LENGTH_SHORT).show()

        // ✅ Clear Fields After Saving
        etAmount.text.clear()
        etDescription.text.clear()
        tvCategoryFilter.text = "Select Category"

        // ✅ Close Activity After Saving
        finish()
    }

    private fun loadExpensesList(): MutableList<Expense> {
        val expensesJson = sharedPreferences.getString("expenses_list", "[]")
        val type = object : TypeToken<MutableList<Expense>>() {}.type
        return gson.fromJson(expensesJson, type) ?: mutableListOf()
    }

    private fun saveExpensesList(expenses: MutableList<Expense>) {
        sharedPreferences.edit().putString("expenses_list", gson.toJson(expenses)).apply()
    }
}
