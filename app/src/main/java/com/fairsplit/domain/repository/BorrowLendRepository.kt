package com.fairsplit.domain.repository

import com.fairsplit.domain.model.BorrowLendTransaction
import com.fairsplit.domain.model.TransactionStatus
import com.fairsplit.domain.model.TransactionType
import com.fairsplit.domain.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Borrow/Lend transaction operations
 */
interface BorrowLendRepository {
    
    /**
     * Add a new borrow/lend transaction
     */
    suspend fun addTransaction(transaction: BorrowLendTransaction): Result<String>
    
    /**
     * Get all transactions for a user
     */
    fun getAllTransactions(userId: String): Flow<List<BorrowLendTransaction>>
    
    /**
     * Get transactions by type (BORROWED or LENT)
     */
    fun getTransactionsByType(userId: String, type: TransactionType): Flow<List<BorrowLendTransaction>>
    
    /**
     * Get transactions by status (PENDING, SETTLED, OVERDUE)
     */
    fun getTransactionsByStatus(userId: String, status: TransactionStatus): Flow<List<BorrowLendTransaction>>
    
    /**
     * Update an existing transaction
     */
    suspend fun updateTransaction(transaction: BorrowLendTransaction): Result<Unit>
    
    /**
     * Delete a transaction
     */
    suspend fun deleteTransaction(transactionId: String): Result<Unit>
    
    /**
     * Mark a transaction as settled
     */
    suspend fun markAsSettled(transactionId: String): Result<Unit>
    
    /**
     * Get total pending amount for a transaction type
     */
    suspend fun getTotalPending(userId: String, type: TransactionType): Result<Double>
    
    /**
     * Search transactions by person name
     */
    fun searchTransactions(userId: String, query: String): Flow<List<BorrowLendTransaction>>
}
