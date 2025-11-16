package com.fairsplit.domain.model

import java.time.YearMonth

/**
 * F25: Archived Month Data
 * Stores historical financial data for previous months
 */
data class ArchivedMonth(
    val id: String,
    val userId: String,
    val yearMonth: YearMonth,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val savingsRate: Float,
    val archivedAt: Long,
    val isRestored: Boolean = false
)

data class ArchiveStats(
    val totalMonths: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val averageSavingsRate: Float,
    val bestMonth: YearMonth?,
    val worstMonth: YearMonth?
)
