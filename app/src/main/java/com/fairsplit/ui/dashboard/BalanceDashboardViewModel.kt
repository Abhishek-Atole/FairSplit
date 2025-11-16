package com.fairsplit.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairsplit.domain.model.TransactionType
import com.fairsplit.domain.repository.BorrowLendRepository
import com.fairsplit.domain.repository.IncomeRepository
import com.fairsplit.domain.repository.ExpenseRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

/**
 * F23: Balance Dashboard ViewModel
 * Aggregates data from Income (F20), Expense (F21), and Borrow/Lend (F22)
 */
@HiltViewModel
class BalanceDashboardViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val borrowLendRepository: BorrowLendRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense.asStateFlow()

    private val _totalBorrowed = MutableStateFlow(0.0)
    val totalBorrowed: StateFlow<Double> = _totalBorrowed.asStateFlow()

    private val _totalLent = MutableStateFlow(0.0)
    val totalLent: StateFlow<Double> = _totalLent.asStateFlow()

    private val _currentBalance = MutableStateFlow(0.0)
    val currentBalance: StateFlow<Double> = _currentBalance.asStateFlow()

    private val _expenseByCategory = MutableStateFlow<Map<String, Double>>(emptyMap())
    val expenseByCategory: StateFlow<Map<String, Double>> = _expenseByCategory.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _dashboardState.value = DashboardState.Error("User not authenticated")
            return
        }

        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            try {
                // Load current month income
                val currentMonth = _selectedMonth.value
                
                when (val incomeResult = incomeRepository.getIncomeByMonth(currentMonth.monthValue, currentMonth.year)) {
                    is Result.Success -> {
                        _totalIncome.value = incomeResult.data?.amount ?: 0.0
                    }
                    is Result.Error -> {
                        _totalIncome.value = 0.0
                    }
                }

                // Load expenses and calculate total + by category
                expenseRepository.getExpensesByMonth(currentMonth.monthValue, currentMonth.year).collect { expenses ->
                    _totalExpense.value = expenses.sumOf { it.amount }
                    
                    // Group by category
                    val categoryMap = expenses.groupBy { it.category.displayName }
                        .mapValues { (_, expenseList) -> expenseList.sumOf { it.amount } }
                    _expenseByCategory.value = categoryMap
                }

                // Load borrow/lend totals
                when (val borrowedResult = borrowLendRepository.getTotalPending(userId, TransactionType.BORROWED)) {
                    is Result.Success -> _totalBorrowed.value = borrowedResult.data
                    is Result.Error -> _totalBorrowed.value = 0.0
                }

                when (val lentResult = borrowLendRepository.getTotalPending(userId, TransactionType.LENT)) {
                    is Result.Success -> _totalLent.value = lentResult.data
                    is Result.Error -> _totalLent.value = 0.0
                }

                // Calculate current balance
                calculateBalance()
                
                _dashboardState.value = DashboardState.Success

            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun calculateBalance() {
        val income = _totalIncome.value
        val expense = _totalExpense.value
        val borrowed = _totalBorrowed.value
        val lent = _totalLent.value
        
        // Balance = Income - Expense + Lent - Borrowed
        // (You lent money OUT, so it reduces your current balance)
        // (You borrowed money IN, so it increases your current balance)
        _currentBalance.value = income - expense + borrowed - lent
    }

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        loadDashboardData()
    }

    fun refreshData() {
        loadDashboardData()
    }
}

sealed class DashboardState {
    object Loading : DashboardState()
    object Success : DashboardState()
    data class Error(val message: String) : DashboardState()
}

data class CategoryExpense(
    val category: String,
    val amount: Double,
    val percentage: Float
)
