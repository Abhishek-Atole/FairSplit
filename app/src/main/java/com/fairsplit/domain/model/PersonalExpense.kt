package com.fairsplit.domain.model

import androidx.annotation.DrawableRes
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Domain model for Personal Expense
 * Represents user's expense linked to monthly income
 */
data class PersonalExpense(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val financeId: String, // Links to MonthlyIncome
    val amount: Double,
    val category: ExpenseCategory,
    val description: String,
    val date: LocalDate,
    val receiptUrl: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

enum class ExpenseCategory(val displayName: String, @DrawableRes val iconRes: Int) {
    FOOD("Food", android.R.drawable.ic_menu_agenda),
    TRANSPORT("Transport", android.R.drawable.ic_menu_directions),
    SHOPPING("Shopping", android.R.drawable.ic_menu_add),
    BILLS("Bills", android.R.drawable.ic_menu_agenda),
    ENTERTAINMENT("Entertainment", android.R.drawable.ic_menu_slideshow),
    OTHER("Other", android.R.drawable.ic_menu_info_details);
    
    companion object {
        fun fromString(value: String): ExpenseCategory {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
