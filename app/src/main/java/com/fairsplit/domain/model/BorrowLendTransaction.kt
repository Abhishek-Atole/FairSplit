package com.fairsplit.domain.model

import java.time.Instant
import java.util.*

/**
 * Domain model for Borrow/Lend transactions
 * Tracks money borrowed from or lent to friends
 */
data class BorrowLendTransaction(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: TransactionType,
    val personName: String,
    val amount: Double,
    val description: String,
    val date: Instant = Instant.now(),
    val status: TransactionStatus = TransactionStatus.PENDING,
    val dueDate: Instant? = null,
    val settledDate: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

enum class TransactionType(val displayName: String) {
    BORROWED("Borrowed"), // You borrowed money from someone
    LENT("Lent");         // You lent money to someone

    companion object {
        fun fromString(value: String): TransactionType {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: BORROWED
        }
    }
}

enum class TransactionStatus(val displayName: String) {
    PENDING("Pending"),
    SETTLED("Settled"),
    OVERDUE("Overdue");

    companion object {
        fun fromString(value: String): TransactionStatus {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}
