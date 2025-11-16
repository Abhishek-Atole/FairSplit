package com.fairsplit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_income")
data class MonthlyIncomeEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val amount: Double,
    val source: String,
    val startDate: Long,
    val endDate: Long?,
    val month: Int,
    val year: Int,
    val createdAt: Long,
    val updatedAt: Long
)
