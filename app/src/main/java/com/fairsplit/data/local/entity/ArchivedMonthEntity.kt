package com.fairsplit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fairsplit.domain.model.ArchivedMonth
import java.time.YearMonth
import java.util.UUID

/**
 * F25: Archived Month Entity
 * Room entity for storing historical financial data
 */
@Entity(tableName = "archived_months")
data class ArchivedMonthEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val year: Int,
    val month: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val savingsRate: Float,
    val archivedAt: Long,
    val isRestored: Boolean = false
)

fun ArchivedMonthEntity.toDomain(): ArchivedMonth {
    return ArchivedMonth(
        id = id,
        userId = userId,
        yearMonth = YearMonth.of(year, month),
        totalIncome = totalIncome,
        totalExpense = totalExpense,
        balance = balance,
        savingsRate = savingsRate,
        archivedAt = archivedAt,
        isRestored = isRestored
    )
}

fun ArchivedMonth.toEntity(): ArchivedMonthEntity {
    return ArchivedMonthEntity(
        id = id,
        userId = userId,
        year = yearMonth.year,
        month = yearMonth.monthValue,
        totalIncome = totalIncome,
        totalExpense = totalExpense,
        balance = balance,
        savingsRate = savingsRate,
        archivedAt = archivedAt,
        isRestored = isRestored
    )
}
