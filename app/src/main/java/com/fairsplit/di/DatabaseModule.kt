package com.fairsplit.di

import android.content.Context
import androidx.room.Room
import com.fairsplit.data.local.FairSplitDatabase
import com.fairsplit.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing Room database and DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideFairSplitDatabase(
        @ApplicationContext context: Context
    ): FairSplitDatabase {
        return Room.databaseBuilder(
            context,
            FairSplitDatabase::class.java,
            "fairsplit_database"
        )
        .fallbackToDestructiveMigration() // TODO: Replace with proper migrations
        .build()
    }
    
    @Provides
    fun provideMonthlyIncomeDao(database: FairSplitDatabase): MonthlyIncomeDao {
        return database.monthlyIncomeDao()
    }
    
    @Provides
    fun providePersonalExpenseDao(database: FairSplitDatabase): PersonalExpenseDao {
        return database.personalExpenseDao()
    }
    
    @Provides
    fun provideBorrowLendTransactionDao(database: FairSplitDatabase): BorrowLendTransactionDao {
        return database.borrowLendTransactionDao()
    }
    
    @Provides
    fun provideArchivedMonthDao(database: FairSplitDatabase): ArchivedMonthDao {
        return database.archivedMonthDao()
    }
}
