package com.fairsplit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fairsplit.domain.model.BorrowLendTransaction
import com.fairsplit.domain.model.TransactionStatus
import com.fairsplit.domain.model.TransactionType
import java.time.Instant

@Entity(tableName = "borrow_lend_transactions")
data class BorrowLendTransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: String, // BORROWED or LENT
    val personName: String,
    val amount: Double,
    val description: String,
    val date: Long, // Timestamp in millis
    val status: String, // PENDING, SETTLED, OVERDUE
    val dueDate: Long? = null, // Timestamp in millis
    val settledDate: Long? = null, // Timestamp in millis
    val createdAt: Long,
    val updatedAt: Long
)

// Extension functions for mapping
fun BorrowLendTransactionEntity.toDomain(): BorrowLendTransaction {
    return BorrowLendTransaction(
        id = id,
        userId = userId,
        type = TransactionType.fromString(type),
        personName = personName,
        amount = amount,
        description = description,
        date = Instant.ofEpochMilli(date),
        status = TransactionStatus.fromString(status),
        dueDate = dueDate?.let { Instant.ofEpochMilli(it) },
        settledDate = settledDate?.let { Instant.ofEpochMilli(it) },
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )
}

fun BorrowLendTransaction.toEntity(): BorrowLendTransactionEntity {
    return BorrowLendTransactionEntity(
        id = id,
        userId = userId,
        type = type.name,
        personName = personName,
        amount = amount,
        description = description,
        date = date.toEpochMilli(),
        status = status.name,
        dueDate = dueDate?.toEpochMilli(),
        settledDate = settledDate?.toEpochMilli(),
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli()
    )
}
