package com.example.data.entity

import androidx.room.Entity

@Entity(tableName = "monthly_budgets", primaryKeys = ["year", "month"])
data class BudgetEntity(
    val year: Int,
    val month: Int, // 1 to 12
    val amount: Long,
    val updatedAt: Long = System.currentTimeMillis()
)
