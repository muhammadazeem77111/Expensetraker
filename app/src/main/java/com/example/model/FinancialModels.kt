package com.example.model

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class TransactionItem(
    val id: Long,
    val type: TransactionType,
    val amount: Long,
    val categoryOrSource: String,
    val description: String,
    val date: Long,
    val createdAt: Long,
    val updatedAt: Long
)

data class CategoryBreakdown(
    val category: String,
    val totalAmount: Long,
    val percentage: Float, // 0.0 to 100.0
    val transactionCount: Int
)

data class MonthlyFinanceSummary(
    val year: Int,
    val month: Int,
    val totalIncome: Long,
    val totalExpenses: Long,
    val balance: Long
)

data class OverallFinanceSummary(
    val totalIncome: Long,
    val totalExpenses: Long,
    val balance: Long
)

data class BudgetStatus(
    val year: Int,
    val month: Int,
    val budgetAmount: Long,
    val spentAmount: Long,
    val remainingAmount: Long,
    val spentPercentage: Float // 0 to 100+
)

data class FilterCriteria(
    val query: String = "",
    val type: TransactionType? = null,
    val category: String? = null,
    val month: Int? = null,
    val year: Int? = null
)

