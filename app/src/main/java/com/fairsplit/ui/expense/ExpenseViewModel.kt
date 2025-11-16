package com.fairsplit.ui.expense

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairsplit.domain.model.ExpenseCategory
import com.fairsplit.domain.model.PersonalExpense
import com.fairsplit.domain.repository.ExpenseRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Idle)
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val _expenseList = MutableStateFlow<List<PersonalExpense>>(emptyList())
    val expenseList: StateFlow<List<PersonalExpense>> = _expenseList.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategory: StateFlow<ExpenseCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadCurrentMonthExpenses()
    }

    fun addExpense(
        amount: Double,
        category: ExpenseCategory,
        description: String,
        date: LocalDate,
        receiptUri: Uri? = null
    ) {
        if (amount <= 0) {
            _uiState.value = ExpenseUiState.Error("Amount must be greater than 0")
            return
        }

        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading

            val userId = auth.currentUser?.uid ?: run {
                _uiState.value = ExpenseUiState.Error("User not authenticated")
                return@launch
            }

            val expense = PersonalExpense(
                id = UUID.randomUUID().toString(),
                userId = userId,
                financeId = "default_finance_${date.year}_${date.monthValue}", // TODO: Link to actual MonthlyIncome.id
                amount = amount,
                category = category,
                description = description,
                date = date,
                receiptUrl = null, // Will be updated after upload
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            when (val result = expenseRepository.addExpense(expense)) {
                is Result.Success -> {
                    val expenseId = result.data
                    
                    // Upload receipt if provided
                    if (receiptUri != null) {
                        uploadReceipt(expenseId, receiptUri)
                    } else {
                        _uiState.value = ExpenseUiState.Success("Expense added successfully")
                        loadCurrentMonthExpenses()
                    }
                }
                is Result.Error -> {
                    _uiState.value = ExpenseUiState.Error(result.message)
                }
            }
        }
    }

    private suspend fun uploadReceipt(expenseId: String, receiptUri: Uri) {
        when (val result = expenseRepository.uploadReceipt(expenseId, receiptUri)) {
            is Result.Success -> {
                _uiState.value = ExpenseUiState.Success("Expense added with receipt")
                loadCurrentMonthExpenses()
            }
            is Result.Error -> {
                _uiState.value = ExpenseUiState.Error("Expense added, but receipt upload failed: ${result.message}")
                loadCurrentMonthExpenses()
            }
        }
    }

    fun loadCurrentMonthExpenses() {
        val now = LocalDate.now()
        loadExpensesForMonth(now.monthValue, now.year)
    }

    fun loadExpensesForMonth(month: Int, year: Int) {
        viewModelScope.launch {
            expenseRepository.getExpensesByMonth(month, year)
                .catch { e ->
                    _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to load expenses")
                }
                .collect { expenses ->
                    _expenseList.value = expenses
                    _uiState.value = ExpenseUiState.Loaded(expenses)
                }
        }
    }

    fun filterByCategory(category: ExpenseCategory?) {
        _selectedCategory.value = category
        
        if (category == null) {
            loadCurrentMonthExpenses()
            return
        }

        viewModelScope.launch {
            expenseRepository.getExpensesByCategory(category)
                .catch { e ->
                    _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to filter expenses")
                }
                .collect { expenses ->
                    _expenseList.value = expenses
                    _uiState.value = ExpenseUiState.Loaded(expenses)
                }
        }
    }

    fun searchExpenses(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            loadCurrentMonthExpenses()
            return
        }

        // Client-side filtering for simplicity
        val filtered = _expenseList.value.filter { expense ->
            expense.description.contains(query, ignoreCase = true) ||
                    expense.category.displayName.contains(query, ignoreCase = true)
        }
        
        _expenseList.value = filtered
        _uiState.value = ExpenseUiState.Loaded(filtered)
    }

    fun loadExpensesByDateRange(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            expenseRepository.getExpensesByDateRange(startDate, endDate)
                .catch { e ->
                    _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to load expenses")
                }
                .collect { expenses ->
                    _expenseList.value = expenses
                    _uiState.value = ExpenseUiState.Loaded(expenses)
                }
        }
    }

    fun updateExpense(expense: PersonalExpense) {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading

            when (val result = expenseRepository.updateExpense(expense)) {
                is Result.Success -> {
                    _uiState.value = ExpenseUiState.Success("Expense updated successfully")
                    loadCurrentMonthExpenses()
                }
                is Result.Error -> {
                    _uiState.value = ExpenseUiState.Error(result.message)
                }
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading

            when (val result = expenseRepository.deleteExpense(expenseId)) {
                is Result.Success -> {
                    _uiState.value = ExpenseUiState.Success("Expense deleted successfully")
                    loadCurrentMonthExpenses()
                }
                is Result.Error -> {
                    _uiState.value = ExpenseUiState.Error(result.message)
                }
            }
        }
    }

    fun getTotalSpent(financeId: String, callback: (Double) -> Unit) {
        viewModelScope.launch {
            when (val result = expenseRepository.getTotalSpent(financeId)) {
                is Result.Success -> callback(result.data)
                is Result.Error -> {
                    _uiState.value = ExpenseUiState.Error(result.message)
                }
            }
        }
    }

    fun clearError() {
        if (_uiState.value is ExpenseUiState.Error) {
            _uiState.value = ExpenseUiState.Idle
        }
    }
}

sealed class ExpenseUiState {
    object Idle : ExpenseUiState()
    object Loading : ExpenseUiState()
    data class Success(val message: String) : ExpenseUiState()
    data class Error(val message: String) : ExpenseUiState()
    data class Loaded(val expenses: List<PersonalExpense>) : ExpenseUiState()
}
