package com.fairsplit.domain.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Domain model for Monthly Income
 * Represents user's income for a specific month
 */
data class MonthlyIncome(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val amount: Double,
    val source: IncomeSource,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val month: Int,
    val year: Int,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

enum class IncomeSource(val displayName: String) {
    SALARY("Salary"),
    BUSINESS("Business"),
    ALLOWANCE("Allowance"),
    OTHER("Other");
    
    companion object {
        fun fromString(value: String): IncomeSource {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
