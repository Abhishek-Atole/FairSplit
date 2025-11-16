package com.fairsplit.domain.repository

import com.fairsplit.domain.model.MonthlyIncome
import com.fairsplit.domain.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Monthly Income operations
 * Follows Clean Architecture principles
 */
interface IncomeRepository {
    
    /**
     * Add a new monthly income entry
     * @return Result with income ID on success
     */
    suspend fun addIncome(income: MonthlyIncome): Result<String>
    
    /**
     * Get income for a specific month and year
     * @return Result with MonthlyIncome or null if not found
     */
    suspend fun getIncomeByMonth(month: Int, year: Int): Result<MonthlyIncome?>
    
    /**
     * Get all income entries for a user
     * @return Flow of income list
     */
    fun getAllIncome(userId: String): Flow<List<MonthlyIncome>>
    
    /**
     * Update an existing income entry
     */
    suspend fun updateIncome(income: MonthlyIncome): Result<Unit>
    
    /**
     * Delete an income entry by ID
     */
    suspend fun deleteIncome(incomeId: String): Result<Unit>
    
    /**
     * Add carry-forward income from previous month
     */
    suspend fun addCarryForwardIncome(
        userId: String,
        month: Int,
        year: Int,
        amount: Double
    ): Result<String>
}
