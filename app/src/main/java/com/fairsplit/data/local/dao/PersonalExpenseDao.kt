package com.fairsplit.data.local.dao

import androidx.room.*
import com.fairsplit.data.local.entity.PersonalExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalExpenseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: PersonalExpenseEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<PersonalExpenseEntity>)
    
    @Update
    suspend fun updateExpense(expense: PersonalExpenseEntity)
    
    @Delete
    suspend fun deleteExpense(expense: PersonalExpenseEntity)
    
    @Query("SELECT * FROM personal_expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: String): PersonalExpenseEntity?
    
    @Query("SELECT * FROM personal_expenses WHERE userId = :userId AND financeId = :financeId ORDER BY date DESC")
    fun getExpensesByFinanceId(userId: String, financeId: String): Flow<List<PersonalExpenseEntity>>
    
    @Query("SELECT * FROM personal_expenses WHERE userId = :userId AND category = :category ORDER BY date DESC")
    fun getExpensesByCategory(userId: String, category: String): Flow<List<PersonalExpenseEntity>>
    
    @Query("SELECT * FROM personal_expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<PersonalExpenseEntity>>
    
    @Query("SELECT * FROM personal_expenses WHERE userId = :userId ORDER BY date DESC")
    fun getAllExpenses(userId: String): Flow<List<PersonalExpenseEntity>>
    
    @Query("SELECT SUM(amount) FROM personal_expenses WHERE userId = :userId AND financeId = :financeId")
    suspend fun getTotalExpenseForFinance(userId: String, financeId: String): Double?
    
    @Query("SELECT SUM(amount) FROM personal_expenses WHERE userId = :userId AND category = :category")
    suspend fun getTotalByCategory(userId: String, category: String): Double?
    
    @Query("SELECT category, SUM(amount) as total FROM personal_expenses WHERE userId = :userId GROUP BY category ORDER BY total DESC")
    suspend fun getCategoryTotals(userId: String): List<CategoryTotal>
    
    @Query("DELETE FROM personal_expenses WHERE userId = :userId AND financeId = :financeId")
    suspend fun deleteExpensesByFinanceId(userId: String, financeId: String)
    
    @Query("SELECT * FROM personal_expenses WHERE userId = :userId AND description LIKE '%' || :searchQuery || '%' ORDER BY date DESC")
    fun searchExpenses(userId: String, searchQuery: String): Flow<List<PersonalExpenseEntity>>
}

/**
 * Data class for category totals query result
 */
data class CategoryTotal(
    val category: String,
    val total: Double
)
