package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BudgetDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.IncomeDao
import com.example.data.entity.BudgetEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.IncomeEntity

@Database(
    entities = [IncomeEntity::class, ExpenseEntity::class, BudgetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expensetrack_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
