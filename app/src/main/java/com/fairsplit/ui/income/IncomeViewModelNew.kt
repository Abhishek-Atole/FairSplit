package com.fairsplit.ui.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairsplit.domain.model.IncomeSource
import com.fairsplit.domain.model.MonthlyIncome
import com.fairsplit.domain.repository.IncomeRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<IncomeUiState>(IncomeUiState.Loading)
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    private val _incomeList = MutableStateFlow<List<MonthlyIncome>>(emptyList())
    val incomeList: StateFlow<List<MonthlyIncome>> = _incomeList.asStateFlow()

    init {
        loadAllIncome()
    }

    fun addIncome(amount: Double, source: IncomeSource, startDate: LocalDate) {
        if (amount <= 0) {
            _uiState.value = IncomeUiState.Error("Amount must be greater than 0")
            return
        }

        viewModelScope.launch {
            _uiState.value = IncomeUiState.Loading
            
            val userId = auth.currentUser?.uid ?: run {
                _uiState.value = IncomeUiState.Error("User not authenticated")
                return@launch
            }

            val endDate = startDate.plusMonths(1).minusDays(1)
            val income = MonthlyIncome(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = amount,
                source = source,
                startDate = startDate,
                endDate = endDate,
                month = startDate.monthValue,
                year = startDate.year,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            when (val result = incomeRepository.addIncome(income)) {
                is Result.Success -> {
                    _uiState.value = IncomeUiState.Success("Income added successfully")
                    loadAllIncome()
                }
                is Result.Error -> {
                    _uiState.value = IncomeUiState.Error(
                        result.message
                    )
                }
            }
        }
    }

    fun loadIncomeForMonth(month: Int, year: Int) {
        viewModelScope.launch {
            _uiState.value = IncomeUiState.Loading
            
            when (val result = incomeRepository.getIncomeByMonth(month, year)) {
                is Result.Success -> {
                    val incomes = listOfNotNull(result.data)
                    _incomeList.value = incomes
                    _uiState.value = IncomeUiState.HistoryLoaded(incomes)
                }
                is Result.Error -> {
                    _uiState.value = IncomeUiState.Error(result.message)
                }
            }
        }
    }

    fun loadAllIncome() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            incomeRepository.getAllIncome(userId)
                .catch { e ->
                    _uiState.value = IncomeUiState.Error(e.message ?: "Failed to load income")
                }
                .collect { incomes ->
                    _incomeList.value = incomes
                    _uiState.value = IncomeUiState.HistoryLoaded(incomes)
                }
        }
    }

    fun updateIncome(income: MonthlyIncome) {
        viewModelScope.launch {
            _uiState.value = IncomeUiState.Loading
            
            when (val result = incomeRepository.updateIncome(income)) {
                is Result.Success -> {
                    _uiState.value = IncomeUiState.Success("Income updated successfully")
                    loadAllIncome()
                }
                is Result.Error -> {
                    _uiState.value = IncomeUiState.Error(
                        result.message
                    )
                }
            }
        }
    }

    fun deleteIncome(incomeId: String) {
        viewModelScope.launch {
            _uiState.value = IncomeUiState.Loading
            
            when (val result = incomeRepository.deleteIncome(incomeId)) {
                is Result.Success -> {
                    _uiState.value = IncomeUiState.Success("Income deleted successfully")
                    loadAllIncome()
                }
                is Result.Error -> {
                    _uiState.value = IncomeUiState.Error(
                        result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        if (_uiState.value is IncomeUiState.Error) {
            _uiState.value = IncomeUiState.Idle
        }
    }
}

sealed class IncomeUiState {
    object Idle : IncomeUiState()
    object Loading : IncomeUiState()
    data class Success(val message: String) : IncomeUiState()
    data class Error(val message: String) : IncomeUiState()
    data class HistoryLoaded(val incomes: List<MonthlyIncome>) : IncomeUiState()
}
