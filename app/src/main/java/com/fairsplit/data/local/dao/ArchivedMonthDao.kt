package com.fairsplit.data.local.dao

import androidx.room.*
import com.fairsplit.data.local.entity.ArchivedMonthEntity
import kotlinx.coroutines.flow.Flow

/**
 * F25: Archived Month DAO
 * Database operations for archived financial data
 */
@Dao
interface ArchivedMonthDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchive(archive: ArchivedMonthEntity)
    
    @Update
    suspend fun updateArchive(archive: ArchivedMonthEntity)
    
    @Delete
    suspend fun deleteArchive(archive: ArchivedMonthEntity)
    
    @Query("SELECT * FROM archived_months WHERE userId = :userId ORDER BY year DESC, month DESC")
    fun getAllArchives(userId: String): Flow<List<ArchivedMonthEntity>>
    
    @Query("SELECT * FROM archived_months WHERE userId = :userId AND year = :year AND month = :month")
    suspend fun getArchiveByMonth(userId: String, year: Int, month: Int): ArchivedMonthEntity?
    
    @Query("SELECT * FROM archived_months WHERE userId = :userId ORDER BY year DESC, month DESC LIMIT 1")
    suspend fun getLatestArchive(userId: String): ArchivedMonthEntity?
    
    @Query("DELETE FROM archived_months WHERE userId = :userId AND year = :year AND month = :month")
    suspend fun deleteArchiveByMonth(userId: String, year: Int, month: Int)
    
    @Query("SELECT COUNT(*) FROM archived_months WHERE userId = :userId")
    suspend fun getArchiveCount(userId: String): Int
    
    @Query("SELECT SUM(totalIncome) FROM archived_months WHERE userId = :userId")
    suspend fun getTotalHistoricalIncome(userId: String): Double?
    
    @Query("SELECT SUM(totalExpense) FROM archived_months WHERE userId = :userId")
    suspend fun getTotalHistoricalExpense(userId: String): Double?
    
    @Query("SELECT AVG(savingsRate) FROM archived_months WHERE userId = :userId")
    suspend fun getAverageSavingsRate(userId: String): Float?
}
