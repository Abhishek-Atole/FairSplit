package com.fairsplit.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairsplit.domain.repository.IncomeRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings ViewModel
 * Handles settings operations including reset income functionality
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _resetState = MutableStateFlow<ResetState>(ResetState.Idle)
    val resetState: StateFlow<ResetState> = _resetState.asStateFlow()

    /**
     * Reset all income entries for the current user
     */
    fun resetAllIncomes() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _resetState.value = ResetState.Error("User not authenticated")
            return
        }

        viewModelScope.launch {
            _resetState.value = ResetState.Loading
            
            try {
                // Get all incomes
                incomeRepository.getAllIncome(userId).collect { incomes ->
                    // Delete each income
                    var allDeleted = true
                    incomes.forEach { income ->
                        when (incomeRepository.deleteIncome(income.id)) {
                            is Result.Success -> { /* Continue */ }
                            is Result.Error -> {
                                allDeleted = false
                            }
                        }
                    }
                    
                    if (allDeleted) {
                        _resetState.value = ResetState.Success("All incomes reset successfully")
                    } else {
                        _resetState.value = ResetState.Error("Some incomes could not be deleted")
                    }
                }
            } catch (e: Exception) {
                _resetState.value = ResetState.Error(e.message ?: "Failed to reset incomes")
            }
        }
    }
}

sealed class ResetState {
    object Idle : ResetState()
    object Loading : ResetState()
    data class Success(val message: String) : ResetState()
    data class Error(val message: String) : ResetState()
}
