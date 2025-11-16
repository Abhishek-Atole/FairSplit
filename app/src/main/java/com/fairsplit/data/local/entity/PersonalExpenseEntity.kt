package com.fairsplit.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a personal expense
 * No foreign key constraint - financeId is a logical reference, not a database constraint
 */
@Entity(
    tableName = "personal_expenses",
    indices = [
        Index(value = ["financeId"]),
        Index(value = ["userId"]),
        Index(value = ["category"]),
        Index(value = ["date"])
    ]
)
data class PersonalExpenseEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val financeId: String,
    val amount: Double,
    val category: String,
    val description: String,
    val date: Long,
    val receiptUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
)
