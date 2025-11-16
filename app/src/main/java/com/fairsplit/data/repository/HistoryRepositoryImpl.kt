package com.fairsplit.data.repository

import com.fairsplit.data.local.dao.ArchivedMonthDao
import com.fairsplit.data.local.dao.PersonalExpenseDao
import com.fairsplit.data.local.dao.MonthlyIncomeDao
import com.fairsplit.data.local.entity.toEntity
import com.fairsplit.data.local.entity.toDomain
import com.fairsplit.domain.model.ArchivedMonth
import com.fairsplit.domain.model.ArchiveStats
import com.fairsplit.domain.repository.HistoryRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject

/**
 * F25: History Repository Implementation
 * Manages archived financial data with dual persistence (Room + Firestore)
 */
class HistoryRepositoryImpl @Inject constructor(
    private val archivedMonthDao: ArchivedMonthDao,
    private val monthlyIncomeDao: MonthlyIncomeDao,
    private val personalExpenseDao: PersonalExpenseDao,
    private val firestore: FirebaseFirestore
) : HistoryRepository {

    companion object {
        private const val COLLECTION_ARCHIVES = "archives"
    }

    override suspend fun archiveMonth(archive: ArchivedMonth): Result<String> {
        return try {
            // Insert to Room
            val entity = archive.toEntity()
            archivedMonthDao.insertArchive(entity)

            // Sync to Firestore
            firestore.collection(COLLECTION_ARCHIVES)
                .document(archive.id)
                .set(
                    mapOf(
                        "userId" to archive.userId,
                        "year" to archive.yearMonth.year,
                        "month" to archive.yearMonth.monthValue,
                        "totalIncome" to archive.totalIncome,
                        "totalExpense" to archive.totalExpense,
                        "balance" to archive.balance,
                        "savingsRate" to archive.savingsRate,
                        "archivedAt" to archive.archivedAt,
                        "isRestored" to archive.isRestored
                    )
                )
                .await()

            Result.Success(archive.id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }

    override fun getAllArchives(userId: String): Flow<List<ArchivedMonth>> {
        return archivedMonthDao.getAllArchives(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getArchiveByMonth(
        userId: String,
        year: Int,
        month: Int
    ): Result<ArchivedMonth?> {
        return try {
            val entity = archivedMonthDao.getArchiveByMonth(userId, year, month)
            Result.Success(entity?.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }

    override suspend fun deleteArchive(archiveId: String): Result<Unit> {
        return try {
            // Delete from Firestore first
            firestore.collection(COLLECTION_ARCHIVES)
                .document(archiveId)
                .delete()
                .await()

            // Delete from Room (we need to fetch entity first to delete)
            // Room requires the entity object for @Delete
            // For now, we'll use a custom query approach
            // This is handled in the DAO level if needed

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }

    override suspend fun restoreArchive(archiveId: String): Result<Unit> {
        return try {
            // Mark as restored in Firestore
            firestore.collection(COLLECTION_ARCHIVES)
                .document(archiveId)
                .update("isRestored", true)
                .await()

            // Note: Actual data restoration logic would need to:
            // 1. Fetch the archived data
            // 2. Re-insert into current income/expense tables
            // This is a placeholder for the restore flag update

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }

    override suspend fun getArchiveStats(userId: String): Result<ArchiveStats> {
        return try {
            val count = archivedMonthDao.getArchiveCount(userId)
            val totalIncome = archivedMonthDao.getTotalHistoricalIncome(userId) ?: 0.0
            val totalExpense = archivedMonthDao.getTotalHistoricalExpense(userId) ?: 0.0
            val avgSavingsRate = archivedMonthDao.getAverageSavingsRate(userId) ?: 0f

            // Note: bestMonth and worstMonth calculation would require
            // fetching all archives and comparing savings rates
            // Placeholder implementation returns null for now
            val stats = ArchiveStats(
                totalMonths = count,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                averageSavingsRate = avgSavingsRate,
                bestMonth = null,
                worstMonth = null
            )

            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }

    override suspend fun performMonthEndReset(userId: String): Result<Unit> {
        return try {
            // Get current month data
            val now = YearMonth.now()
            val previousMonth = now.minusMonths(1)
            val year = previousMonth.year
            val month = previousMonth.monthValue

            // Calculate totals from DAOs
            val totalIncome = monthlyIncomeDao.getTotalIncomeForMonth(userId, month, year) ?: 0.0
            
            // Calculate total expenses for previous month using date range
            val startOfMonth = previousMonth.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfMonth = previousMonth.atEndOfMonth().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() + (24 * 60 * 60 * 1000 - 1)
            
            // Query expenses for the previous month
            val expenses = personalExpenseDao.getExpensesByDateRange(userId, startOfMonth, endOfMonth).first()
            val totalExpense = expenses.sumOf { it.amount }
            
            val balance = totalIncome - totalExpense
            val savingsRate = if (totalIncome > 0) {
                ((totalIncome - totalExpense) / totalIncome * 100).toFloat()
            } else 0f

            val archive = ArchivedMonth(
                id = UUID.randomUUID().toString(),
                userId = userId,
                yearMonth = previousMonth,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                balance = balance,
                savingsRate = savingsRate,
                archivedAt = System.currentTimeMillis(),
                isRestored = false
            )

            // Archive the month
            archiveMonth(archive)

            // Note: Optionally clear current month data here
            // This depends on business requirements

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }
}
