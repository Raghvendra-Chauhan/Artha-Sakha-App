package com.example.arthasakha.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.arthasakha.R
import com.example.arthasakha.models.Expense
import com.example.arthasakha.EditExpenseActivity

class ExpensesAdapter(
    private val context: Context,
    private var expenseList: MutableList<Expense>,
    private val onExpenseClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvExpenseDateTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]

        holder.tvAmount.text = "₹${expense.amount}"
        holder.tvCategory.text = expense.category
        holder.tvDescription.text = expense.description
        holder.tvDateTime.text = expense.date

        if (expense.isUPITransaction) {
            // Highlight UPI transactions
            holder.tvCategory.text = "UPI Transaction"
            holder.tvCategory.setTextColor(Color.BLUE)
            holder.itemView.setBackgroundColor(Color.LTGRAY)

            // Prevent editing for UPI transactions
            holder.itemView.setOnClickListener {
                Toast.makeText(context, "UPI Transactions cannot be edited", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Normal expenses are editable
            holder.itemView.setOnClickListener {
                val intent = Intent(context, EditExpenseActivity::class.java)
                intent.putExtra("EXPENSE_ID", expense.id)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = expenseList.size

    fun updateList(newList: List<Expense>) {
        expenseList.clear()
        expenseList.addAll(newList)
        notifyDataSetChanged()
    }
}
