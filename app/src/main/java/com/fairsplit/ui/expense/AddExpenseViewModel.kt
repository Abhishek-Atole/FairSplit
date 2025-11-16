package com.fairsplit.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairsplit.domain.model.ExpenseCategory
import com.fairsplit.domain.model.PersonalExpense
import com.fairsplit.domain.repository.ExpenseRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Add Expense screen
 * Handles expense creation with proper DI and database persistence
 */
@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddExpenseUiState>(AddExpenseUiState.Idle)
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _validationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val validationErrors: StateFlow<Map<String, String>> = _validationErrors.asStateFlow()

    /**
     * Add a new expense
     */
    fun addExpense(
        amount: Double,
        category: ExpenseCategory,
        description: String,
        date: LocalDate = LocalDate.now()
    ) {
        viewModelScope.launch {
            // Validate input
            val errors = mutableMapOf<String, String>()
            
            if (amount <= 0) {
                errors["amount"] = "Amount must be greater than 0"
            }
            
            if (description.isBlank()) {
                errors["description"] = "Description is required"
            }
            
            if (errors.isNotEmpty()) {
                _validationErrors.value = errors
                _uiState.value = AddExpenseUiState.Error("Please fix the validation errors")
                return@launch
            }
            
            _validationErrors.value = emptyMap()
            _uiState.value = AddExpenseUiState.Loading
            
            val userId = auth.currentUser?.uid 
            if (userId == null) {
                _uiState.value = AddExpenseUiState.Error("User not authenticated")
                return@launch
            }
            
            val expense = PersonalExpense(
                id = UUID.randomUUID().toString(),
                userId = userId,
                financeId = userId, // For now, financeId = userId for personal expenses
                amount = amount,
                category = category,
                description = description,
                date = date,
                receiptUrl = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            when (val result = expenseRepository.addExpense(expense)) {
                is Result.Success -> {
                    _uiState.value = AddExpenseUiState.Success(expense)
                }
                is Result.Error -> {
                    _uiState.value = AddExpenseUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Reset UI state
     */
    fun resetState() {
        _uiState.value = AddExpenseUiState.Idle
        _validationErrors.value = emptyMap()
    }

    /**
     * Clear validation error for a specific field
     */
    fun clearError(field: String) {
        _validationErrors.value = _validationErrors.value.toMutableMap().apply {
            remove(field)
        }
    }
}

/**
 * UI state for Add Expense screen
 */
sealed class AddExpenseUiState {
    data object Idle : AddExpenseUiState()
    data object Loading : AddExpenseUiState()
    data class Success(val expense: PersonalExpense) : AddExpenseUiState()
    data class Error(val message: String) : AddExpenseUiState()
}
