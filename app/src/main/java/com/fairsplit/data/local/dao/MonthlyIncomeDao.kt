package com.fairsplit.data.local.dao

import androidx.room.*
import com.fairsplit.data.local.entity.MonthlyIncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyIncomeDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: MonthlyIncomeEntity)
    
    @Update
    suspend fun updateIncome(income: MonthlyIncomeEntity)
    
    @Delete
    suspend fun deleteIncome(income: MonthlyIncomeEntity)
    
    @Query("SELECT * FROM monthly_income WHERE id = :incomeId")
    suspend fun getIncomeById(incomeId: String): MonthlyIncomeEntity?
    
    @Query("SELECT * FROM monthly_income WHERE userId = :userId AND month = :month AND year = :year ORDER BY startDate DESC")
    fun getIncomeByMonth(userId: String, month: Int, year: Int): Flow<List<MonthlyIncomeEntity>>
    
    @Query("SELECT * FROM monthly_income WHERE userId = :userId ORDER BY year DESC, month DESC, startDate DESC")
    fun getAllIncome(userId: String): Flow<List<MonthlyIncomeEntity>>
    
    @Query("SELECT * FROM monthly_income WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestIncome(userId: String): MonthlyIncomeEntity?
    
    @Query("DELETE FROM monthly_income WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun deleteIncomeByMonth(userId: String, month: Int, year: Int)
    
    @Query("SELECT SUM(amount) FROM monthly_income WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getTotalIncomeForMonth(userId: String, month: Int, year: Int): Double?
}
