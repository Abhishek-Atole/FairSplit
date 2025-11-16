package com.fairsplit

import android.app.Application
import androidx.work.*
import androidx.hilt.work.HiltWorkerFactory
import com.fairsplit.worker.AutoResetWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltAndroidApp
class FairSplitApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    override fun onCreate() {
        super.onCreate()
        
        // Schedule auto-reset worker for monthly archiving
        scheduleMonthlyAutoReset()
    }
    
    private fun scheduleMonthlyAutoReset() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<AutoResetWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(calculateDelayToNextMonth(), TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(AutoResetWorker.WORK_NAME)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            AutoResetWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    private fun calculateDelayToNextMonth(): Long {
        val now = LocalDateTime.now()
        val firstDayOfNextMonth = now.toLocalDate().plusMonths(1).withDayOfMonth(1)
        val targetDateTime = firstDayOfNextMonth.atTime(0, 1)
        
        return ChronoUnit.MILLIS.between(
            now.atZone(ZoneId.systemDefault()).toInstant(),
            targetDateTime.atZone(ZoneId.systemDefault()).toInstant()
        )
    }
}
