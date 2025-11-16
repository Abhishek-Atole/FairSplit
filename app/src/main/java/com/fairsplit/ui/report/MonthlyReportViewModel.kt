package com.fairsplit.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairsplit.domain.model.*
import com.fairsplit.domain.repository.BorrowLendRepository
import com.fairsplit.domain.repository.ExpenseRepository
import com.fairsplit.domain.repository.IncomeRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * F24: Monthly Report ViewModel
 * Generates comprehensive monthly financial reports
 */
@HiltViewModel
class MonthlyReportViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val borrowLendRepository: BorrowLendRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _reportState = MutableStateFlow<ReportState>(ReportState.Loading)
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

    private val _monthlyReport = MutableStateFlow<MonthlyReport?>(null)
    val monthlyReport: StateFlow<MonthlyReport?> = _monthlyReport.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    init {
        generateReport()
    }

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        generateReport()
    }

    fun generateReport() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _reportState.value = ReportState.Error("User not authenticated")
            return
        }

        viewModelScope.launch {
            _reportState.value = ReportState.Loading
            try {
                val currentMonth = _selectedMonth.value
                
                // Get income
                var totalIncome = 0.0
                when (val incomeResult = incomeRepository.getIncomeByMonth(
                    currentMonth.monthValue,
                    currentMonth.year
                )) {
                    is Result.Success -> {
                        totalIncome = incomeResult.data?.amount ?: 0.0
                    }
                    is Result.Error -> {
                        totalIncome = 0.0
                    }
                }

                // Get expenses
                var totalExpense = 0.0
                var expenseByCategory = mapOf<String, CategorySummary>()
                var topExpenses = listOf<ExpenseItem>()
                
                expenseRepository.getExpensesByMonth(
                    currentMonth.monthValue,
                    currentMonth.year
                ).collect { expenses ->
                    totalExpense = expenses.sumOf { it.amount }
                    
                    // Group by category
                    val categoryGroups = expenses.groupBy { it.category.displayName }
                    expenseByCategory = categoryGroups.mapValues { (categoryName, expenseList) ->
                        val categoryTotal = expenseList.sumOf { it.amount }
                        CategorySummary(
                            categoryName = categoryName,
                            totalAmount = categoryTotal,
                            percentage = if (totalExpense > 0) 
                                (categoryTotal / totalExpense * 100).toFloat() else 0f,
                            transactionCount = expenseList.size
                        )
                    }
                    
                    // Get top 5 expenses
                    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                    topExpenses = expenses
                        .sortedByDescending { it.amount }
                        .take(5)
                        .map { expense ->
                            ExpenseItem(
                                description = expense.description,
                                category = expense.category.displayName,
                                amount = expense.amount,
                                date = expense.date.format(dateFormatter)
                            )
                        }
                }

                // Get borrow/lend totals
                var borrowedAmount = 0.0
                var lentAmount = 0.0
                
                when (val borrowedResult = borrowLendRepository.getTotalPending(
                    userId,
                    TransactionType.BORROWED
                )) {
                    is Result.Success -> borrowedAmount = borrowedResult.data
                    is Result.Error -> borrowedAmount = 0.0
                }

                when (val lentResult = borrowLendRepository.getTotalPending(
                    userId,
                    TransactionType.LENT
                )) {
                    is Result.Success -> lentAmount = lentResult.data
                    is Result.Error -> lentAmount = 0.0
                }

                // Calculate metrics
                val balance = totalIncome - totalExpense
                val savingsRate = if (totalIncome > 0) 
                    ((balance / totalIncome) * 100).toFloat() else 0f
                val netWorth = totalIncome - totalExpense - borrowedAmount + lentAmount

                val report = MonthlyReport(
                    yearMonth = currentMonth,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = balance,
                    savingsRate = savingsRate,
                    expenseByCategory = expenseByCategory,
                    topExpenses = topExpenses,
                    borrowedAmount = borrowedAmount,
                    lentAmount = lentAmount,
                    netWorth = netWorth
                )

                _monthlyReport.value = report
                _reportState.value = ReportState.Success

            } catch (e: Exception) {
                _reportState.value = ReportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun exportToPdf() {
        viewModelScope.launch {
            // TODO: Implement PDF export
            _reportState.value = ReportState.Success
        }
    }

    fun shareReport() {
        viewModelScope.launch {
            // TODO: Implement share functionality
        }
    }
}

sealed class ReportState {
    object Loading : ReportState()
    object Success : ReportState()
    data class Error(val message: String) : ReportState()
}
