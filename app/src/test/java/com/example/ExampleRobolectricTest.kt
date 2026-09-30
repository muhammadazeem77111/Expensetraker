package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.IncomeEntity
import com.example.data.repository.ExpenseTrackRepository
import com.example.util.FinanceFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: ExpenseTrackRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ExpenseTrackRepository(
            incomeDao = database.incomeDao(),
            expenseDao = database.expenseDao(),
            budgetDao = database.budgetDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun verifyAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ExpenseTrack", appName)
    }

    @Test
    fun verifyRequiredFinancialSequence_Test1ToTest10() = runBlocking {
        val now = System.currentTimeMillis()

        // TEST 1: Fresh installation
        // Expected: Income = Rs. 0, Expenses = Rs. 0, Balance = Rs. 0
        var summary = repository.overallSummary.first()
        assertEquals(0L, summary.totalIncome)
        assertEquals(0L, summary.totalExpenses)
        assertEquals(0L, summary.balance)
        assertEquals("Rs. 0", FinanceFormatter.formatCurrency(summary.balance))

        // TEST 2: Add Income Rs. 60,000
        // Expected: Income = Rs. 60,000, Expenses = Rs. 0, Balance = Rs. 60,000
        val incomeId = repository.insertIncome(
            IncomeEntity(
                amount = 60000L,
                source = "Salary",
                description = "September Salary",
                date = now,
                createdAt = now,
                updatedAt = now
            )
        )
        summary = repository.overallSummary.first()
        assertEquals(60000L, summary.totalIncome)
        assertEquals(0L, summary.totalExpenses)
        assertEquals(60000L, summary.balance)
        assertEquals("Rs. 60,000", FinanceFormatter.formatCurrency(summary.balance))

        // TEST 3 & 4: Inactive / reopen test - values remain identical
        summary = repository.overallSummary.first()
        assertEquals(60000L, summary.totalIncome)
        assertEquals(0L, summary.totalExpenses)
        assertEquals(60000L, summary.balance)

        // TEST 5: Add Expense Rs. 15,000
        // Expected: Income = Rs. 60,000, Expenses = Rs. 15,000, Balance = Rs. 45,000
        val expenseId = repository.insertExpense(
            ExpenseEntity(
                amount = 15000L,
                category = "Food",
                description = "Grocery and Dining",
                date = now,
                createdAt = now,
                updatedAt = now
            )
        )
        summary = repository.overallSummary.first()
        assertEquals(60000L, summary.totalIncome)
        assertEquals(15000L, summary.totalExpenses)
        assertEquals(45000L, summary.balance)
        assertEquals("Rs. 45,000", FinanceFormatter.formatCurrency(summary.balance))

        // TEST 8: Edit the Rs. 15,000 expense to Rs. 10,000
        // Expected: Income = Rs. 60,000, Expenses = Rs. 10,000, Balance = Rs. 50,000
        // There must still be only ONE expense record.
        repository.updateExpense(
            ExpenseEntity(
                id = expenseId,
                amount = 10000L,
                category = "Food",
                description = "Grocery and Dining - Updated",
                date = now,
                createdAt = now,
                updatedAt = System.currentTimeMillis()
            )
        )
        val allExpenses = database.expenseDao().getAllExpenses().first()
        assertEquals(1, allExpenses.size)
        assertEquals(10000L, allExpenses[0].amount)

        summary = repository.overallSummary.first()
        assertEquals(60000L, summary.totalIncome)
        assertEquals(10000L, summary.totalExpenses)
        assertEquals(50000L, summary.balance)
        assertEquals("Rs. 50,000", FinanceFormatter.formatCurrency(summary.balance))

        // TEST 9: Delete the Rs. 10,000 expense
        // Expected: Income = Rs. 60,000, Expenses = Rs. 0, Balance = Rs. 60,000
        repository.deleteExpenseById(expenseId)
        val afterDeleteExpenses = database.expenseDao().getAllExpenses().first()
        assertEquals(0, afterDeleteExpenses.size)

        summary = repository.overallSummary.first()
        assertEquals(60000L, summary.totalIncome)
        assertEquals(0L, summary.totalExpenses)
        assertEquals(60000L, summary.balance)
        assertEquals("Rs. 60,000", FinanceFormatter.formatCurrency(summary.balance))

        // TEST 10: Inactivity / Balance remains stable
        summary = repository.overallSummary.first()
        assertEquals(60000L, summary.balance)
    }
}
