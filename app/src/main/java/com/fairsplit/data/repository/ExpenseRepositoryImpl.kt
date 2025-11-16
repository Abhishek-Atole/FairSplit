package com.fairsplit.data.repository

import android.net.Uri
import com.fairsplit.data.local.dao.PersonalExpenseDao
import com.fairsplit.data.local.entity.PersonalExpenseEntity
import com.fairsplit.domain.model.ExpenseCategory
import com.fairsplit.domain.model.PersonalExpense
import com.fairsplit.domain.repository.ExpenseRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: PersonalExpenseDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : ExpenseRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

    override suspend fun addExpense(expense: PersonalExpense): Result<String> {
        return try {
            val entity = expense.toEntity()
            
            // Save to local database FIRST (primary storage)
            expenseDao.insertExpense(entity)
            
            // Try to sync to Firestore (secondary, optional)
            try {
                val data = mapOf(
                    "id" to expense.id,
                    "userId" to expense.userId,
                    "financeId" to expense.financeId,
                    "amount" to expense.amount,
                    "category" to expense.category.name.lowercase(),
                    "description" to expense.description,
                    "date" to com.google.firebase.Timestamp(Date(entity.date)),
                    "receiptUrl" to expense.receiptUrl,
                    "createdAt" to com.google.firebase.Timestamp(Date(entity.createdAt)),
                    "updatedAt" to com.google.firebase.Timestamp(Date(entity.updatedAt))
                )
                
                // Simplified Firestore path: expenses/{userId}/items/{expenseId}
                firestore.collection("expenses")
                    .document(userId)
                    .collection("items")
                    .document(expense.id)
                    .set(data)
                    .await()
            } catch (firestoreError: Exception) {
                // Log but don't fail - Room database is primary storage
                android.util.Log.w("ExpenseRepository", "Firestore sync failed: ${firestoreError.message}")
            }
            
            // Return success as long as Room save worked
            Result.Success(expense.id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add expense")
        }
    }

    override fun getExpensesByMonth(month: Int, year: Int): Flow<List<PersonalExpense>> {
        // Calculate date range for month
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        return expenseDao.getExpensesByDateRange(userId, startMillis, endMillis)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getExpensesByCategory(category: ExpenseCategory): Flow<List<PersonalExpense>> {
        return expenseDao.getExpensesByCategory(userId, category.name)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<PersonalExpense>> {
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        return expenseDao.getExpensesByDateRange(userId, startMillis, endMillis)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getTotalSpent(financeId: String): Result<Double> {
        return try {
            val total = expenseDao.getTotalExpenseForFinance(userId, financeId) ?: 0.0
            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get total spent")
        }
    }

    override suspend fun getTotalSpent(userId: String, month: Int, year: Int): Result<Double> {
        return try {
            // Calculate date range for month
            val startDate = LocalDate.of(year, month, 1)
            val endDate = startDate.plusMonths(1).minusDays(1)
            val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            var total = 0.0
            val expenses = expenseDao.getExpensesByDateRange(userId, startMillis, endMillis)
            expenses.collect { list -> total = list.sumOf { it.amount } }
            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get monthly total")
        }
    }

    override suspend fun updateExpense(expense: PersonalExpense): Result<Unit> {
        return try {
            val entity = expense.copy(updatedAt = Instant.now()).toEntity()
            
            expenseDao.updateExpense(entity)
            
            // Sync to Firestore
            firestore.collection("finance")
                .document("users")
                .collection(userId)
                .document("personal_expenses")
                .collection("items")
                .document(expense.id)
                .update(
                    mapOf(
                        "amount" to expense.amount,
                        "category" to expense.category.name.lowercase(),
                        "description" to expense.description,
                        "date" to com.google.firebase.Timestamp(Date(entity.date)),
                        "receiptUrl" to expense.receiptUrl,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Operation failed")
        }
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            val entity = expenseDao.getExpenseById(expenseId)
            if (entity != null) {
                // Delete receipt from storage if exists
                entity.receiptUrl?.let { url ->
                    try {
                        storage.getReferenceFromUrl(url).delete().await()
                    } catch (e: Exception) {
                        // Log but don't fail if receipt deletion fails
                    }
                }
                
                expenseDao.deleteExpense(entity)
                
                // Delete from Firestore
                firestore.collection("finance")
                    .document("users")
                    .collection(userId)
                    .document("personal_expenses")
                    .collection("items")
                    .document(expenseId)
                    .delete()
                    .await()
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Operation failed")
        }
    }

    override suspend fun uploadReceipt(expenseId: String, imageUri: Uri): Result<String> {
        return try {
            val fileName = "receipts/${userId}/${expenseId}_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child(fileName)
            
            val uploadTask = storageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            
            // Update expense with receipt URL
            val expense = expenseDao.getExpenseById(expenseId)
            if (expense != null) {
                val updatedExpense = expense.copy(
                    receiptUrl = downloadUrl,
                    updatedAt = System.currentTimeMillis()
                )
                expenseDao.updateExpense(updatedExpense)
                
                // Update Firestore
                firestore.collection("finance")
                    .document("users")
                    .collection(userId)
                    .document("personal_expenses")
                    .collection("items")
                    .document(expenseId)
                    .update("receiptUrl", downloadUrl)
                    .await()
            }
            
            Result.Success(downloadUrl)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Operation failed")
        }
    }

    // Extension functions for mapping
    private fun PersonalExpense.toEntity(): PersonalExpenseEntity {
        return PersonalExpenseEntity(
            id = id,
            userId = userId,
            financeId = financeId,
            amount = amount,
            category = category.name,
            description = description,
            date = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            receiptUrl = receiptUrl,
            createdAt = createdAt.toEpochMilli(),
            updatedAt = updatedAt.toEpochMilli()
        )
    }

    private fun PersonalExpenseEntity.toDomain(): PersonalExpense {
        return PersonalExpense(
            id = id,
            userId = userId,
            financeId = financeId,
            amount = amount,
            category = ExpenseCategory.valueOf(category.uppercase()),
            description = description,
            date = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate(),
            receiptUrl = receiptUrl,
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt)
        )
    }
}
