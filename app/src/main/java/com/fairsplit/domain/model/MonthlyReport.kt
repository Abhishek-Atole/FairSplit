package com.fairsplit.domain.model

import java.time.YearMonth

/**
 * F24: Monthly Report
 * Comprehensive financial report for a specific month
 */
data class MonthlyReport(
    val yearMonth: YearMonth,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val savingsRate: Float, // Percentage
    val expenseByCategory: Map<String, CategorySummary>,
    val topExpenses: List<ExpenseItem>,
    val borrowedAmount: Double,
    val lentAmount: Double,
    val netWorth: Double // Income - Expense - Borrowed + Lent
)

data class CategorySummary(
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Float,
    val transactionCount: Int
)

data class ExpenseItem(
    val description: String,
    val category: String,
    val amount: Double,
    val date: String
)
