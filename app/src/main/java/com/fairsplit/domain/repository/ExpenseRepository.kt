package com.fairsplit.domain.repository

import android.net.Uri
import com.fairsplit.domain.model.ExpenseCategory
import com.fairsplit.domain.model.PersonalExpense
import com.fairsplit.domain.util.Result
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for Personal Expense operations
 */
interface ExpenseRepository {
    
    /**
     * Add a new expense entry
     */
    suspend fun addExpense(expense: PersonalExpense): Result<String>
    
    /**
     * Get expenses for a specific month
     */
    fun getExpensesByMonth(month: Int, year: Int): Flow<List<PersonalExpense>>
    
    /**
     * Get expenses filtered by category
     */
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<PersonalExpense>>
    
    /**
     * Get expenses within date range
     */
    fun getExpensesByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<PersonalExpense>>
    
    /**
     * Calculate total spent for a finance period
     */
    suspend fun getTotalSpent(financeId: String): Result<Double>
    
    /**
     * Calculate total spent for a specific month/year
     */
    suspend fun getTotalSpent(userId: String, month: Int, year: Int): Result<Double>
    
    /**
     * Update an existing expense
     */
    suspend fun updateExpense(expense: PersonalExpense): Result<Unit>
    
    /**
     * Delete an expense
     */
    suspend fun deleteExpense(expenseId: String): Result<Unit>
    
    /**
     * Upload receipt image to cloud storage
     * @return Result with storage URL
     */
    suspend fun uploadReceipt(expenseId: String, imageUri: Uri): Result<String>
}
