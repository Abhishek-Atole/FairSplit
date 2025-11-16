package com.fairsplit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairsplit.domain.model.ArchivedMonth
import com.fairsplit.domain.model.ArchiveStats
import com.fairsplit.domain.repository.HistoryRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F25: History ViewModel
 * Manages UI state for archived financial data
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    // UI State
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    // Archives list from repository
    val archives: StateFlow<List<ArchivedMonth>> = historyRepository
        .getAllArchives(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Archive statistics
    private val _archiveStats = MutableStateFlow<ArchiveStats?>(null)
    val archiveStats: StateFlow<ArchiveStats?> = _archiveStats.asStateFlow()

    init {
        loadArchiveStats()
    }

    /**
     * Load historical statistics
     */
    fun loadArchiveStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = historyRepository.getArchiveStats(userId)) {
                is Result.Success -> {
                    _archiveStats.value = result.data
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load stats"
                        )
                    }
                }
            }
        }
    }

    /**
     * Manually trigger month-end archive
     */
    fun archiveCurrentMonth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = historyRepository.performMonthEndReset(userId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null,
                            successMessage = "Month archived successfully"
                        )
                    }
                    loadArchiveStats()
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to archive month"
                        )
                    }
                }
            }
        }
    }

    /**
     * Delete an archived month
     */
    fun deleteArchive(archiveId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = historyRepository.deleteArchive(archiveId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null,
                            successMessage = "Archive deleted"
                        )
                    }
                    loadArchiveStats()
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to delete archive"
                        )
                    }
                }
            }
        }
    }

    /**
     * Restore archived month data
     */
    fun restoreArchive(archiveId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = historyRepository.restoreArchive(archiveId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null,
                            successMessage = "Archive restored"
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to restore archive"
                        )
                    }
                }
            }
        }
    }

    /**
     * Get specific archive by month
     */
    fun getArchiveByMonth(year: Int, month: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = historyRepository.getArchiveByMonth(userId, year, month)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            selectedArchive = result.data,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load archive"
                        )
                    }
                }
            }
        }
    }

    /**
     * Clear success/error messages
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    /**
     * Clear selected archive
     */
    fun clearSelection() {
        _uiState.update { it.copy(selectedArchive = null) }
    }
}

/**
 * UI State for History Screen
 */
data class HistoryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedArchive: ArchivedMonth? = null
)
