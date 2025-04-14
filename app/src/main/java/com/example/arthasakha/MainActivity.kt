// File: MainActivity.kt

package com.example.arthasakha

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arthasakha.adapters.ExpensesAdapter
import com.example.arthasakha.models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var txtWelcome: TextView
    private lateinit var txtSubtitle: TextView
    private lateinit var profileIcon: ImageView
    private lateinit var addExpenseBox: LinearLayout
    private lateinit var qrScannerBox: LinearLayout
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var txtShowMore: TextView
    private lateinit var txtBudget: TextView
    private lateinit var txtSpent: TextView
    private lateinit var txtRemaining: TextView
    private lateinit var barChart: BarChart
    private lateinit var txtEmptyExpenses: TextView  // Add this at the top with other views


    private val expenseList = mutableListOf<Expense>()
    private var isGuestUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("expenses_prefs", Context.MODE_PRIVATE)

        initViews()
        setupRecyclerView()
        detectUserMode()
        setListeners()
        refreshUI()
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun initViews() {
        txtWelcome = findViewById(R.id.txtWelcome)
        txtSubtitle = findViewById(R.id.txtSubtitle)
        profileIcon = findViewById(R.id.profileIcon)
        addExpenseBox = findViewById(R.id.btnAddExpense)
        qrScannerBox = findViewById(R.id.btnScanQR)
        expensesRecyclerView = findViewById(R.id.rvRecentExpenses)
        txtShowMore = findViewById(R.id.txtShowMore)
        txtBudget = findViewById(R.id.txtBudget)
        txtSpent = findViewById(R.id.txtSpent)
        txtRemaining = findViewById(R.id.txtRemaining)
        barChart = findViewById(R.id.barChart)
        txtEmptyExpenses = findViewById(R.id.txtEmptyExpenses)

    }

    private fun setupRecyclerView() {
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)
        expensesAdapter = ExpensesAdapter(this, expenseList) { expense ->
            navigateToEditExpense(expense)
        }
        expensesRecyclerView.adapter = expensesAdapter
    }

    private fun detectUserMode() {
        isGuestUser = intent.getBooleanExtra("GUEST_MODE", false)
        val currentUser = auth.currentUser

        val name = if (isGuestUser || currentUser == null) {
            "Guest"
        } else {
            currentUser.displayName ?: "User"
        }

        txtWelcome.text = "Welcome, $name"
        txtSubtitle.text = "to Artha Sakha"
    }

    private fun refreshUI() {
        loadExpenses()
        updateExpenseSummary()
        updateBarChart()
    }

    private fun loadExpenses() {
        val gson = Gson()
        val json = sharedPreferences.getString("expenses_list", "[]") ?: "[]"
        val type = object : TypeToken<List<Expense>>() {}.type
        val expenses: List<Expense> = gson.fromJson(json, type) ?: emptyList()

        expenseList.clear()
        expenseList.addAll(expenses.reversed())
        expensesAdapter.notifyDataSetChanged()

        val isEmpty = expenseList.isEmpty()
        txtShowMore.visibility = if (isEmpty) View.GONE else View.VISIBLE
        expensesRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        txtEmptyExpenses.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }


    private fun updateExpenseSummary() {
        val gson = Gson()
        val json = sharedPreferences.getString("expenses_list", "[]") ?: "[]"
        val type = object : TypeToken<List<Expense>>() {}.type
        val expenses: List<Expense> = gson.fromJson(json, type) ?: emptyList()

        val budget = SharedPreferencesHelper.getBudget(this)
        val spent = expenses.sumOf { it.amount }
        val remaining = budget - spent

        txtBudget.text = "Budget: ₹${budget.toInt()}"
        txtSpent.text = "Spent: ₹${spent.toInt()}"
        txtRemaining.text = "Remaining: ₹${remaining.toInt()}"
    }

    private fun updateBarChart() {
        val gson = Gson()
        val json = sharedPreferences.getString("expenses_list", "[]") ?: "[]"
        val type = object : TypeToken<List<Expense>>() {}.type
        val expenses: List<Expense> = gson.fromJson(json, type) ?: emptyList()

        val categoryMap = mapOf(
            "Food" to expenses.filter { it.category == "Food" }.sumOf { it.amount },
            "Travel" to expenses.filter { it.category == "Travel" }.sumOf { it.amount },
            "Shopping" to expenses.filter { it.category == "Shopping" }.sumOf { it.amount },
            "Entertainment" to expenses.filter { it.category == "Entertainment" }.sumOf { it.amount },
            "Other" to expenses.filter { it.category == "Other" }.sumOf { it.amount }
        )

        val entries = categoryMap.entries.mapIndexed { idx, entry ->
            BarEntry(idx.toFloat(), entry.value.toFloat())
        }

        val dataSet = BarDataSet(entries, "Expense Categories").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
        }

        barChart.apply {
            data = BarData(dataSet)
            description.text = "Expenses by Category"
            animateY(1000)
            invalidate()
        }
    }

    private fun setListeners() {
        profileIcon.setOnClickListener { showProfileMenu(it) }
        addExpenseBox.setOnClickListener { startActivity(Intent(this, AddExpenseActivity::class.java)) }
        qrScannerBox.setOnClickListener { startActivityForResult(Intent(this, QRScannerActivity::class.java), 1001) }
        txtShowMore.setOnClickListener { startActivity(Intent(this, AllExpensesActivity::class.java)) }
    }

    private fun showProfileMenu(view: View) {
        PopupMenu(this, view).apply {
            menuInflater.inflate(R.menu.profile_menu, menu)
            if (isGuestUser) {
                menu.findItem(R.id.menu_logout).title = "Login"
            }
            setOnMenuItemClickListener { onMenuItemClicked(it) }
        }.show()
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                handleLogout()
                true
            }
            else -> false
        }
    }

    private fun handleLogout() {
        if (!isGuestUser) auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun navigateToEditExpense(expense: Expense) {
        val intent = Intent(this, EditExpenseActivity::class.java).apply {
            putExtra("EXPENSE_ID", expense.id)
            putExtra("EXPENSE_TITLE", expense.title)
            putExtra("EXPENSE_AMOUNT", expense.amount.toString())
            putExtra("EXPENSE_CATEGORY", expense.category)
        }
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            val qr = data?.getStringExtra("SCANNED_QR")
            qr?.let { handleQRData(it) }
        }
    }

    private fun handleQRData(qrData: String) {
        if (qrData.startsWith("upi://")) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(qrData))
            startActivity(intent)
        } else {
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_LONG).show()
        }
    }
}
