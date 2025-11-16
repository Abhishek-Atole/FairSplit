package com.fairsplit.di

import com.fairsplit.data.repository.*
import com.fairsplit.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding repository implementations to interfaces
 * Uses @Binds for efficient dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindIncomeRepository(
        incomeRepositoryImpl: IncomeRepositoryImpl
    ): IncomeRepository
    
    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository
    
    @Binds
    @Singleton
    abstract fun bindBorrowLendRepository(
        borrowLendRepositoryImpl: BorrowLendRepositoryImpl
    ): BorrowLendRepository
    
    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        historyRepositoryImpl: HistoryRepositoryImpl
    ): HistoryRepository
}
