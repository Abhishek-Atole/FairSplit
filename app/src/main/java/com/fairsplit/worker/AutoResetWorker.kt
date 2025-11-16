package com.fairsplit.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fairsplit.domain.repository.HistoryRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.util.Log

/**
 * WorkManager worker for automatic monthly reset and archiving
 * Runs on the 1st of each month
 */
@HiltWorker
class AutoResetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val historyRepository: HistoryRepository,
    private val auth: FirebaseAuth
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "AutoResetWorker"
        const val WORK_NAME = "monthly_auto_reset"
    }
    
    override suspend fun doWork(): Result {
        return try {
            val userId = auth.currentUser?.uid
            
            if (userId == null) {
                Log.e(TAG, "User not authenticated, skipping auto-reset")
                return Result.failure()
            }
            
            Log.d(TAG, "Starting monthly auto-reset for authenticated user")
            
            // Perform month-end reset through repository
            when (val result = historyRepository.performMonthEndReset(userId)) {
                is com.fairsplit.domain.util.Result.Success -> {
                    Log.d(TAG, "Monthly auto-reset completed successfully")
                    
                    // TODO: Send notification to user
                    // NotificationHelper.showArchiveNotification(applicationContext)
                    
                    Result.success()
                }
                is com.fairsplit.domain.util.Result.Error -> {
                    Log.e(TAG, "Monthly auto-reset failed", result.exception)
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during auto-reset", e)
            Result.retry()
        }
    }
}
