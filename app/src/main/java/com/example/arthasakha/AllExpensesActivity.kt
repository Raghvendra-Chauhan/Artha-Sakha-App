package com.example.arthasakha

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arthasakha.adapters.ExpensesAdapter
import com.example.arthasakha.models.Expense
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class AllExpensesActivity : AppCompatActivity() {

    private lateinit var rvAllExpenses: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var btnFilter: ImageView
    private lateinit var searchView: SearchView
    private lateinit var tvAppliedFilter: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private lateinit var expensesAdapter: ExpensesAdapter
    private var expensesList: MutableList<Expense> = mutableListOf()
    private var filteredList: MutableList<Expense> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_expenses)

        // Initialize UI components
        rvAllExpenses = findViewById(R.id.rvAllExpenses)
        btnBack = findViewById(R.id.btnBack)
        btnFilter = findViewById(R.id.btnFilter)
        searchView = findViewById(R.id.searchView)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvAppliedFilter = findViewById(R.id.tvAppliedFilter)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("expenses_prefs", Context.MODE_PRIVATE)

        // Load and display all expenses
        loadAllExpenses()

        // Handle Back Button
        btnBack.setOnClickListener { finish() }

        // Handle Search View
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterExpenses(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterExpenses(newText ?: "")
                return true
            }
        })

        // Handle Filter Button
        btnFilter.setOnClickListener { showFilterDialog() }
    }

    private fun loadAllExpenses() {
        val expensesJson = sharedPreferences.getString("expenses_list", "[]") ?: "[]"
        val type = object : TypeToken<MutableList<Expense>>() {}.type
        expensesList = gson.fromJson(expensesJson, type) ?: mutableListOf()

        if (expensesList.isNotEmpty()) {
            // Sort expenses by date (latest first)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            expensesList.sortByDescending {
                runCatching { dateFormat.parse(it.date) }.getOrNull()
            }

            // Set up adapter
            filteredList = expensesList.toMutableList()
            rvAllExpenses.layoutManager = LinearLayoutManager(this)
            expensesAdapter = ExpensesAdapter(this, filteredList) { expense ->
                openEditExpenseScreen(expense)
            }
            rvAllExpenses.adapter = expensesAdapter
        } else {
            Toast.makeText(this, "No expenses found!", Toast.LENGTH_SHORT).show()
        }

        // Update total expenses
        tvTotalExpenses.text = "Total: ₹${calculateTotalExpenses()}"
        tvAppliedFilter.text = "Showing: All Expenses"
    }

    private fun filterExpenses(query: String) {
        filteredList = expensesList.filter { expense ->
            (expense.title?.contains(query, ignoreCase = true) ?: false) ||
                    (expense.category?.contains(query, ignoreCase = true) ?: false) ||
                    (expense.amount.toString().contains(query))
        }.toMutableList()

        expensesAdapter.updateList(filteredList)
    }

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.filter_expenses_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Initialize UI components
        val tvCategory = dialogView.findViewById<TextView>(R.id.tvCategoryFilter) // Replace Spinner with TextView
        val rgDateFilter = dialogView.findViewById<RadioGroup>(R.id.rgDateFilter)
        val rgAmountFilter = dialogView.findViewById<RadioGroup>(R.id.rgAmountFilter)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnApply = dialogView.findViewById<Button>(R.id.btnApplyFilters)
        val btnClearFilters = dialogView.findViewById<Button>(R.id.btnClearFilters)

        // Set default category text
        tvCategory.text = "Select Category"

        // Open category selection dialog
        tvCategory.setOnClickListener {
            showCategoryDialog(tvCategory)
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnApply.setOnClickListener {
            val selectedCategory = tvCategory.text.toString()
            val selectedDateFilter = when (rgDateFilter.checkedRadioButtonId) {
                R.id.rbLast7Days -> 7
                R.id.rbLast30Days -> 30
                else -> null
            }
            val selectedAmountFilter = when (rgAmountFilter.checkedRadioButtonId) {
                R.id.rbAbove1000 -> "Above 1000"
                R.id.rbBelow1000 -> "Below 1000"
                else -> null
            }

            applyFilters(selectedCategory, selectedDateFilter, selectedAmountFilter)
            dialog.dismiss()
        }

        // Clear Filters Button
        btnClearFilters.setOnClickListener {
            tvCategory.text = "Select Category"
            rgDateFilter.clearCheck()
            rgAmountFilter.clearCheck()
            filteredList = expensesList.toMutableList()
            expensesAdapter.updateList(filteredList)
            tvAppliedFilter.text = "Showing: All Expenses"
            dialog.dismiss()
        }
        // ✅ Clear Filters Button
        btnClearFilters.setOnClickListener {
            rgDateFilter.clearCheck()
            rgAmountFilter.clearCheck()
            filteredList = expensesList.toMutableList()
            expensesAdapter.updateList(filteredList)
            tvAppliedFilter.text = "Showing: All Expenses"
            dialog.dismiss()
        }


        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showCategoryDialog(tvCategory: TextView) {
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
            tvCategory.text = categories[position] // Set selected category in TextView
            dialog.dismiss()
        }

        dialog.show()
    }




    private fun applyFilters(category: String, dateRange: Int?, amountFilter: String?) {
        filteredList = expensesList.filter { expense ->
            val matchesCategory = category == "All" || expense.category == category
            val matchesDate = dateRange?.let { filterByDate(expense.date, it) } ?: true
            val matchesAmount = when (amountFilter) {
                "Above 1000" -> expense.amount > 1000
                "Below 1000" -> expense.amount <= 1000
                else -> true
            }
            matchesCategory && matchesDate && matchesAmount
        }.toMutableList()

        expensesAdapter.updateList(filteredList)

        // Update the applied filter text
        tvAppliedFilter.text = "Filtered Expenses"
    }

    private fun filterByDate(date: String, days: Int): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val cutoffDate = calendar.time

        return runCatching { dateFormat.parse(date) }.getOrNull()?.after(cutoffDate) ?: false
    }

    private fun openEditExpenseScreen(expense: Expense) {
        val intent = Intent(this, EditExpenseActivity::class.java)
        intent.putExtra("EXPENSE_ID", expense.id)
        startActivity(intent)
    }

    private fun calculateTotalExpenses(): Double {
        return expensesList.sumOf { it.amount }
    }
}
