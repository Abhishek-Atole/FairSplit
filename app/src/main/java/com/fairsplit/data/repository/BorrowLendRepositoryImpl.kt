package com.fairsplit.data.repository

import com.fairsplit.data.local.dao.BorrowLendTransactionDao
import com.fairsplit.data.local.entity.toEntity
import com.fairsplit.data.local.entity.toDomain
import com.fairsplit.domain.model.BorrowLendTransaction
import com.fairsplit.domain.model.TransactionStatus
import com.fairsplit.domain.model.TransactionType
import com.fairsplit.domain.repository.BorrowLendRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject

class BorrowLendRepositoryImpl @Inject constructor(
    private val transactionDao: BorrowLendTransactionDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : BorrowLendRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

    override suspend fun addTransaction(transaction: BorrowLendTransaction): Result<String> {
        return try {
            val entity = transaction.toEntity()
            
            // Save to local database
            transactionDao.insertTransaction(entity)
            
            // Sync to Firestore
            val data = mapOf(
                "id" to transaction.id,
                "userId" to transaction.userId,
                "type" to transaction.type.name,
                "personName" to transaction.personName,
                "amount" to transaction.amount,
                "description" to transaction.description,
                "date" to com.google.firebase.Timestamp(java.util.Date(entity.date)),
                "status" to transaction.status.name,
                "dueDate" to transaction.dueDate?.let { com.google.firebase.Timestamp(java.util.Date(entity.dueDate!!)) },
                "settledDate" to transaction.settledDate?.let { com.google.firebase.Timestamp(java.util.Date(entity.settledDate!!)) },
                "createdAt" to com.google.firebase.Timestamp(java.util.Date(entity.createdAt)),
                "updatedAt" to com.google.firebase.Timestamp(java.util.Date(entity.updatedAt))
            )
            
            firestore.collection("finance")
                .document("users")
                .collection(userId)
                .document("borrow_lend")
                .collection("transactions")
                .document(transaction.id)
                .set(data)
                .await()
            
            Result.Success(transaction.id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add transaction")
        }
    }

    override fun getAllTransactions(userId: String): Flow<List<BorrowLendTransaction>> {
        return transactionDao.getAllTransactions(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getTransactionsByType(
        userId: String,
        type: TransactionType
    ): Flow<List<BorrowLendTransaction>> {
        return transactionDao.getTransactionsByType(userId, type.name)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getTransactionsByStatus(
        userId: String,
        status: TransactionStatus
    ): Flow<List<BorrowLendTransaction>> {
        return transactionDao.getTransactionsByStatus(userId, status.name)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun updateTransaction(transaction: BorrowLendTransaction): Result<Unit> {
        return try {
            val entity = transaction.copy(updatedAt = Instant.now()).toEntity()
            
            transactionDao.updateTransaction(entity)
            
            // Sync to Firestore
            firestore.collection("finance")
                .document("users")
                .collection(userId)
                .document("borrow_lend")
                .collection("transactions")
                .document(transaction.id)
                .update(
                    mapOf(
                        "personName" to transaction.personName,
                        "amount" to transaction.amount,
                        "description" to transaction.description,
                        "status" to transaction.status.name,
                        "dueDate" to transaction.dueDate?.let { com.google.firebase.Timestamp(java.util.Date(entity.dueDate!!)) },
                        "settledDate" to transaction.settledDate?.let { com.google.firebase.Timestamp(java.util.Date(entity.settledDate!!)) },
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update transaction")
        }
    }

    override suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            val entity = transactionDao.getTransactionById(transactionId)
            if (entity != null) {
                transactionDao.deleteTransaction(entity)
                
                // Delete from Firestore
                firestore.collection("finance")
                    .document("users")
                    .collection(userId)
                    .document("borrow_lend")
                    .collection("transactions")
                    .document(transactionId)
                    .delete()
                    .await()
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete transaction")
        }
    }

    override suspend fun markAsSettled(transactionId: String): Result<Unit> {
        return try {
            val now = Instant.now().toEpochMilli()
            
            transactionDao.markAsSettled(transactionId, now, now)
            
            // Sync to Firestore
            firestore.collection("finance")
                .document("users")
                .collection(userId)
                .document("borrow_lend")
                .collection("transactions")
                .document(transactionId)
                .update(
                    mapOf(
                        "status" to "SETTLED",
                        "settledDate" to com.google.firebase.Timestamp.now(),
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to mark as settled")
        }
    }

    override suspend fun getTotalPending(userId: String, type: TransactionType): Result<Double> {
        return try {
            val total = transactionDao.getTotalPendingAmount(userId, type.name) ?: 0.0
            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to calculate total")
        }
    }

    override fun searchTransactions(userId: String, query: String): Flow<List<BorrowLendTransaction>> {
        return transactionDao.searchTransactions(userId, query)
            .map { entities -> entities.map { it.toDomain() } }
    }
}
