package com.fairsplit.domain.repository

import com.fairsplit.domain.model.ArchivedMonth
import com.fairsplit.domain.model.ArchiveStats
import com.fairsplit.domain.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * F25: History Repository
 * Manages archived financial data and month-end automation
 */
interface HistoryRepository {
    
    /**
     * Archive current month data
     */
    suspend fun archiveMonth(archive: ArchivedMonth): Result<String>
    
    /**
     * Get all archived months
     */
    fun getAllArchives(userId: String): Flow<List<ArchivedMonth>>
    
    /**
     * Get archive for specific month
     */
    suspend fun getArchiveByMonth(userId: String, year: Int, month: Int): Result<ArchivedMonth?>
    
    /**
     * Delete archived month
     */
    suspend fun deleteArchive(archiveId: String): Result<Unit>
    
    /**
     * Restore archived month data
     */
    suspend fun restoreArchive(archiveId: String): Result<Unit>
    
    /**
     * Get historical statistics
     */
    suspend fun getArchiveStats(userId: String): Result<ArchiveStats>
    
    /**
     * Perform month-end reset
     * Archives current data and prepares for new month
     */
    suspend fun performMonthEndReset(userId: String): Result<Unit>
}
