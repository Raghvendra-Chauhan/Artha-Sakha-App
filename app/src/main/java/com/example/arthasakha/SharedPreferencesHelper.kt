package com.example.arthasakha

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper object to manage SharedPreferences for storing and retrieving budget data.
 */
object SharedPreferencesHelper {

    private const val PREFS_NAME = "expenses_prefs"       // Consistent name
    private const val KEY_BUDGET = "budget"
    private const val BUDGET_AMOUNT = "budget_amount"     // Used in isBudgetSet()

    fun isBudgetSet(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains(KEY_BUDGET) || prefs.contains(BUDGET_AMOUNT)
    }

    fun saveBudget(context: Context, budget: Int) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_BUDGET, budget).apply()
    }

    fun getBudget(context: Context): Int {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_BUDGET, 0) // Default is 0
    }

    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
