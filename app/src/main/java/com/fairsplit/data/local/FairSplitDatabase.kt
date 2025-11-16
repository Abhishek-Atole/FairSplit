package com.fairsplit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fairsplit.data.local.dao.MonthlyIncomeDao
import com.fairsplit.data.local.dao.PersonalExpenseDao
import com.fairsplit.data.local.dao.BorrowLendTransactionDao
import com.fairsplit.data.local.dao.ArchivedMonthDao
import com.fairsplit.data.local.entity.MonthlyIncomeEntity
import com.fairsplit.data.local.entity.PersonalExpenseEntity
import com.fairsplit.data.local.entity.BorrowLendTransactionEntity
import com.fairsplit.data.local.entity.ArchivedMonthEntity

@Database(
    entities = [
        MonthlyIncomeEntity::class,
        PersonalExpenseEntity::class,
        BorrowLendTransactionEntity::class,
        ArchivedMonthEntity::class,
    ],
    version = 2, // Incremented to remove foreign key constraint
    exportSchema = false
)
abstract class FairSplitDatabase : RoomDatabase() {
    abstract fun monthlyIncomeDao(): MonthlyIncomeDao
    abstract fun personalExpenseDao(): PersonalExpenseDao
    abstract fun borrowLendTransactionDao(): BorrowLendTransactionDao
    abstract fun archivedMonthDao(): ArchivedMonthDao

    companion object {
        const val DATABASE_NAME = "fairsplit_database"
    }
}
