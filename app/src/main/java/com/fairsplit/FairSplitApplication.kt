package com.fairsplit

import android.app.Application
import androidx.work.*
import androidx.hilt.work.HiltWorkerFactory
import com.fairsplit.worker.AutoResetWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import java.time.LocalDate
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
            .addTag("auto_reset_monthly")
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "auto_reset_monthly",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    private fun calculateDelayToNextMonth(): Long {
        val now = LocalDate.now()
        val firstDayOfNextMonth = now.plusMonths(1).withDayOfMonth(1)
        val targetDateTime = firstDayOfNextMonth.atTime(0, 1)
        val nowDateTime = now.atStartOfDay()
        
        return ChronoUnit.MILLIS.between(
            nowDateTime.atZone(ZoneId.systemDefault()).toInstant(),
            targetDateTime.atZone(ZoneId.systemDefault()).toInstant()
        )
    }
}
