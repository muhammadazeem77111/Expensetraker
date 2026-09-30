package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: BudgetEntity)

    @Query("SELECT * FROM monthly_budgets WHERE year = :year AND month = :month LIMIT 1")
    fun getBudget(year: Int, month: Int): Flow<BudgetEntity?>

    @Query("DELETE FROM monthly_budgets")
    suspend fun clearAllBudgets()
}
