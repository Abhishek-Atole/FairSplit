package com.fairsplit.data.repository

import com.fairsplit.data.local.dao.MonthlyIncomeDao
import com.fairsplit.data.local.entity.MonthlyIncomeEntity
import com.fairsplit.domain.model.MonthlyIncome
import com.fairsplit.domain.repository.IncomeRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

class IncomeRepositoryImpl @Inject constructor(
    private val incomeDao: MonthlyIncomeDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IncomeRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

    override suspend fun addIncome(income: MonthlyIncome): Result<String> {
        return try {
            val entity = income.toEntity()
            
            // Save to local database
            incomeDao.insertIncome(entity)
            
            // Sync to Firestore
            val data = mapOf(
                "id" to income.id,
                "userId" to income.userId,
                "amount" to income.amount,
                "source" to income.source.name.lowercase(),
                "startDate" to com.google.firebase.Timestamp(Date(entity.startDate)),
                "endDate" to income.endDate?.let { com.google.firebase.Timestamp(Date(entity.endDate!!)) },
                "month" to income.month,
                "year" to income.year,
                "createdAt" to com.google.firebase.Timestamp(Date(entity.createdAt)),
                "updatedAt" to com.google.firebase.Timestamp(Date(entity.updatedAt))
            )
            
            firestore.collection("finance")
                .document("users")
                .collection(userId)
                .document("monthly_income")
                .collection("items")
                .document(income.id)
                .set(data)
                .await()
            
            Result.Success(income.id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Operation failed")
        }
    }

    override suspend fun getIncomeByMonth(month: Int, year: Int): Result<MonthlyIncome?> {
        return try {
            // Get from Flow and take first emission
            val entities = incomeDao.getIncomeByMonth(userId, month, year)
            var result: MonthlyIncome? = null
            entities.collect { list ->
                result = list.firstOrNull()?.toDomain()
            }
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get income by month")
        }
    }

    override fun getAllIncome(userId: String): Flow<List<MonthlyIncome>> {
        return incomeDao.getAllIncome(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun updateIncome(income: MonthlyIncome): Result<Unit> {
        return try {
            val entity = income.copy(updatedAt = Instant.now()).toEntity()
            
            incomeDao.updateIncome(entity)
            
            // Sync to Firestore
            firestore.collection("finance")
                .document("users")
                .collection(userId)
                .document("monthly_income")
                .collection("items")
                .document(income.id)
                .update(
                    mapOf(
                        "amount" to income.amount,
                        "source" to income.source.name.lowercase(),
                        "startDate" to com.google.firebase.Timestamp(Date(entity.startDate)),
                        "endDate" to income.endDate?.let { com.google.firebase.Timestamp(Date(entity.endDate!!)) },
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Operation failed")
        }
    }

    override suspend fun deleteIncome(incomeId: String): Result<Unit> {
        return try {
            val entity = incomeDao.getIncomeById(incomeId)
            if (entity != null) {
                incomeDao.deleteIncome(entity)
                
                // Delete from Firestore
                firestore.collection("finance")
                    .document("users")
                    .collection(userId)
                    .document("monthly_income")
                    .collection("items")
                    .document(incomeId)
                    .delete()
                    .await()
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Operation failed")
        }
    }

    override suspend fun addCarryForwardIncome(
        userId: String,
        month: Int,
        year: Int,
        amount: Double
    ): Result<String> {
        return try {
            val carryForwardIncome = MonthlyIncome(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = amount,
                source = com.fairsplit.domain.model.IncomeSource.OTHER,
                startDate = LocalDate.of(year, month, 1),
                endDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1),
                month = month,
                year = year,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            addIncome(carryForwardIncome)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Operation failed")
        }
    }

    // Extension functions for mapping
    private fun MonthlyIncome.toEntity(): MonthlyIncomeEntity {
        return MonthlyIncomeEntity(
            id = id,
            userId = userId,
            amount = amount,
            source = source.name,
            startDate = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endDate = endDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            month = month,
            year = year,
            createdAt = createdAt.toEpochMilli(),
            updatedAt = updatedAt.toEpochMilli()
        )
    }

    private fun MonthlyIncomeEntity.toDomain(): MonthlyIncome {
        return MonthlyIncome(
            id = id,
            userId = userId,
            amount = amount,
            source = com.fairsplit.domain.model.IncomeSource.valueOf(source.uppercase()),
            startDate = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate(),
            endDate = endDate?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() },
            month = month,
            year = year,
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt)
        )
    }
}
