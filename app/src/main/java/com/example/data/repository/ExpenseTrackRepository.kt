package com.example.data.repository

import com.example.data.dao.BudgetDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.IncomeDao
import com.example.data.entity.BudgetEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.IncomeEntity
import com.example.model.BudgetStatus
import com.example.model.CategoryBreakdown
import com.example.model.MonthlyFinanceSummary
import com.example.model.OverallFinanceSummary
import com.example.model.TransactionItem
import com.example.model.TransactionType
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ExpenseTrackRepository(
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao
) {

    // --- OVERALL FINANCIAL SUMMARY ---
    // Calculated directly from database aggregations
    val overallSummary: Flow<OverallFinanceSummary> = combine(
        incomeDao.getTotalIncome(),
        expenseDao.getTotalExpenses()
    ) { income, expense ->
        OverallFinanceSummary(
            totalIncome = income,
            totalExpenses = expense,
            balance = income - expense
        )
    }

    // --- ALL TRANSACTIONS (REACTIVE STREAM) ---
    val allTransactions: Flow<List<TransactionItem>> = combine(
        incomeDao.getAllIncome(),
        expenseDao.getAllExpenses()
    ) { incomes, expenses ->
        val incomeItems = incomes.map {
            TransactionItem(
                id = it.id,
                type = TransactionType.INCOME,
                amount = it.amount,
                categoryOrSource = it.source,
                description = it.description,
                date = it.date,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }
        val expenseItems = expenses.map {
            TransactionItem(
                id = it.id,
                type = TransactionType.EXPENSE,
                amount = it.amount,
                categoryOrSource = it.category,
                description = it.description,
                date = it.date,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }
        // Strict ordering by date DESC, then createdAt DESC
        (incomeItems + expenseItems).sortedWith(
            compareByDescending<TransactionItem> { it.date }.thenByDescending { it.createdAt }
        )
    }

    // --- MONTHLY SUMMARY ---
    fun getMonthlySummary(year: Int, month: Int): Flow<MonthlyFinanceSummary> {
        val (start, end) = DateUtils.getMonthDateRange(year, month)
        return combine(
            incomeDao.getTotalIncomeForDateRange(start, end),
            expenseDao.getTotalExpensesForDateRange(start, end)
        ) { income, expense ->
            MonthlyFinanceSummary(
                year = year,
                month = month,
                totalIncome = income,
                totalExpenses = expense,
                balance = income - expense
            )
        }
    }

    // --- MONTHLY TRANSACTIONS ---
    fun getMonthlyTransactions(year: Int, month: Int): Flow<List<TransactionItem>> {
        val (start, end) = DateUtils.getMonthDateRange(year, month)
        return combine(
            incomeDao.getIncomeForDateRange(start, end),
            expenseDao.getExpensesForDateRange(start, end)
        ) { incomes, expenses ->
            val incomeItems = incomes.map {
                TransactionItem(
                    id = it.id,
                    type = TransactionType.INCOME,
                    amount = it.amount,
                    categoryOrSource = it.source,
                    description = it.description,
                    date = it.date,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
            val expenseItems = expenses.map {
                TransactionItem(
                    id = it.id,
                    type = TransactionType.EXPENSE,
                    amount = it.amount,
                    categoryOrSource = it.category,
                    description = it.description,
                    date = it.date,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
            (incomeItems + expenseItems).sortedWith(
                compareByDescending<TransactionItem> { it.date }.thenByDescending { it.createdAt }
            )
        }
    }

    // --- CATEGORY BREAKDOWN (FOR REPORTS) ---
    fun getCategoryBreakdown(year: Int, month: Int): Flow<List<CategoryBreakdown>> {
        val (start, end) = DateUtils.getMonthDateRange(year, month)
        return expenseDao.getExpensesForDateRange(start, end).map { expenses ->
            if (expenses.isEmpty()) {
                emptyList()
            } else {
                val totalSpent = expenses.sumOf { it.amount }
                expenses.groupBy { it.category }
                    .map { (cat, list) ->
                        val catTotal = list.sumOf { it.amount }
                        val percentage = if (totalSpent > 0) {
                            (catTotal.toFloat() / totalSpent.toFloat()) * 100f
                        } else {
                            0f
                        }
                        CategoryBreakdown(
                            category = cat,
                            totalAmount = catTotal,
                            percentage = percentage,
                            transactionCount = list.size
                        )
                    }
                    .sortedByDescending { it.totalAmount }
            }
        }
    }

    // --- BUDGET STATUS ---
    // Budget is strictly reference/limit. NEVER creates expenses or deducts money.
    fun getBudgetStatus(year: Int, month: Int): Flow<BudgetStatus> {
        val (start, end) = DateUtils.getMonthDateRange(year, month)
        return combine(
            budgetDao.getBudget(year, month),
            expenseDao.getTotalExpensesForDateRange(start, end)
        ) { budgetEntity, totalExpenses ->
            val budgetAmount = budgetEntity?.amount ?: 0L
            val spent = totalExpenses
            val remaining = budgetAmount - spent
            val percentage = if (budgetAmount > 0) {
                (spent.toFloat() / budgetAmount.toFloat()) * 100f
            } else {
                0f
            }
            BudgetStatus(
                year = year,
                month = month,
                budgetAmount = budgetAmount,
                spentAmount = spent,
                remainingAmount = remaining,
                spentPercentage = percentage
            )
        }
    }

    // --- CRUD INCOME ---
    suspend fun insertIncome(income: IncomeEntity): Long {
        return incomeDao.insertIncome(income)
    }

    suspend fun updateIncome(income: IncomeEntity) {
        incomeDao.updateIncome(income)
    }

    suspend fun deleteIncomeById(id: Long): Boolean {
        return incomeDao.deleteIncomeById(id) > 0
    }

    suspend fun getIncomeById(id: Long): IncomeEntity? {
        return incomeDao.getIncomeById(id)
    }

    // --- CRUD EXPENSE ---
    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long): Boolean {
        return expenseDao.deleteExpenseById(id) > 0
    }

    suspend fun getExpenseById(id: Long): ExpenseEntity? {
        return expenseDao.getExpenseById(id)
    }

    // --- BUDGET ---
    suspend fun setBudget(year: Int, month: Int, amount: Long) {
        budgetDao.setBudget(
            BudgetEntity(
                year = year,
                month = month,
                amount = amount,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // --- CLEAR ALL DATA ---
    suspend fun clearAllData() {
        incomeDao.clearAllIncome()
        expenseDao.clearAllExpenses()
        budgetDao.clearAllBudgets()
    }
}
