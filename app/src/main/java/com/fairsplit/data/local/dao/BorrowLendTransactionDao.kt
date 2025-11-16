package com.fairsplit.data.local.dao

import androidx.room.*
import com.fairsplit.data.local.entity.BorrowLendTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BorrowLendTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: BorrowLendTransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: BorrowLendTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: BorrowLendTransactionEntity)

    @Query("SELECT * FROM borrow_lend_transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: String): Flow<List<BorrowLendTransactionEntity>>

    @Query("SELECT * FROM borrow_lend_transactions WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getTransactionsByType(userId: String, type: String): Flow<List<BorrowLendTransactionEntity>>

    @Query("SELECT * FROM borrow_lend_transactions WHERE userId = :userId AND status = :status ORDER BY date DESC")
    fun getTransactionsByStatus(userId: String, status: String): Flow<List<BorrowLendTransactionEntity>>

    @Query("SELECT * FROM borrow_lend_transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): BorrowLendTransactionEntity?

    @Query("SELECT SUM(amount) FROM borrow_lend_transactions WHERE userId = :userId AND type = :type AND status = 'PENDING'")
    suspend fun getTotalPendingAmount(userId: String, type: String): Double?

    @Query("SELECT * FROM borrow_lend_transactions WHERE userId = :userId AND personName LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchTransactions(userId: String, query: String): Flow<List<BorrowLendTransactionEntity>>

    @Query("UPDATE borrow_lend_transactions SET status = 'SETTLED', settledDate = :settledDate, updatedAt = :updatedAt WHERE id = :transactionId")
    suspend fun markAsSettled(transactionId: String, settledDate: Long, updatedAt: Long)
}
