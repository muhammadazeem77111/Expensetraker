package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.IncomeEntity
import com.example.data.repository.ExpenseTrackRepository
import com.example.model.BudgetStatus
import com.example.model.CategoryBreakdown
import com.example.model.FilterCriteria
import com.example.model.MonthlyFinanceSummary
import com.example.model.OverallFinanceSummary
import com.example.model.TransactionItem
import com.example.model.TransactionType
import com.example.util.DateUtils
import com.example.util.FinanceFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseTrackViewModel(
    private val repository: ExpenseTrackRepository
) : ViewModel() {

    // Current selected month and year for reports and budget
    private val _selectedYear = MutableStateFlow(DateUtils.getCurrentYear())
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(DateUtils.getCurrentMonth())
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Transaction search and filters
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow<TransactionType?>(null) // null = ALL
    val filterCategory = MutableStateFlow<String?>(null)
    val filterMonth = MutableStateFlow<Int?>(null) // null = all months
    val filterYear = MutableStateFlow<Int?>(null) // null = all years

    // Guard against duplicate inserts/clicks
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Status message / toast / snackbar
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // --- OVERALL FINANCIAL SUMMARY ---
    // Purely derived from Room Database SUM queries.
    val overallSummary: StateFlow<OverallFinanceSummary> = repository.overallSummary
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OverallFinanceSummary(0L, 0L, 0L)
        )

    // Current month summary (for dashboard banner)
    val currentMonthSummary: StateFlow<MonthlyFinanceSummary> =
        repository.getMonthlySummary(DateUtils.getCurrentYear(), DateUtils.getCurrentMonth())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = MonthlyFinanceSummary(
                    year = DateUtils.getCurrentYear(),
                    month = DateUtils.getCurrentMonth(),
                    totalIncome = 0L,
                    totalExpenses = 0L,
                    balance = 0L
                )
            )

    // Summary for currently selected month in Reports/Budget
    val selectedMonthSummary: StateFlow<MonthlyFinanceSummary> = combine(
        _selectedYear,
        _selectedMonth
    ) { year, month -> Pair(year, month) }
        .flatMapLatest { (year, month) ->
            repository.getMonthlySummary(year, month)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MonthlyFinanceSummary(
                year = DateUtils.getCurrentYear(),
                month = DateUtils.getCurrentMonth(),
                totalIncome = 0L,
                totalExpenses = 0L,
                balance = 0L
            )
        )

    // All transactions from Room
    val allTransactions: StateFlow<List<TransactionItem>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Recent 5 transactions for Dashboard
    val recentTransactions: StateFlow<List<TransactionItem>> = allTransactions
        .map { it.take(5) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val filterCriteria = combine(
        searchQuery,
        filterType,
        filterCategory,
        filterMonth,
        filterYear
    ) { query, type, category, month, year ->
        FilterCriteria(query, type, category, month, year)
    }

    // Filtered transactions for Transactions screen
    val filteredTransactions: StateFlow<List<TransactionItem>> = combine(
        allTransactions,
        filterCriteria
    ) { transactions, criteria ->
        transactions.filter { item ->
            // Search query filter
            val matchesQuery = if (criteria.query.isBlank()) {
                true
            } else {
                item.description.contains(criteria.query, ignoreCase = true) ||
                        item.categoryOrSource.contains(criteria.query, ignoreCase = true) ||
                        item.amount.toString().contains(criteria.query)
            }

            // Type filter
            val matchesType = criteria.type == null || item.type == criteria.type

            // Category filter
            val matchesCategory = criteria.category == null || item.categoryOrSource.equals(criteria.category, ignoreCase = true)

            // Month and Year filter
            val matchesDate = if (criteria.month != null || criteria.year != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = item.date }
                val itemMonth = cal.get(Calendar.MONTH) + 1
                val itemYear = cal.get(Calendar.YEAR)
                (criteria.month == null || itemMonth == criteria.month) && (criteria.year == null || itemYear == criteria.year)
            } else {
                true
            }

            matchesQuery && matchesType && matchesCategory && matchesDate
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reports: Category breakdown for selected month
    val categoryBreakdown: StateFlow<List<CategoryBreakdown>> = combine(
        _selectedYear,
        _selectedMonth
    ) { year, month -> Pair(year, month) }
        .flatMapLatest { (year, month) ->
            repository.getCategoryBreakdown(year, month)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Budget: Budget status for selected month
    val budgetStatus: StateFlow<BudgetStatus> = combine(
        _selectedYear,
        _selectedMonth
    ) { year, month -> Pair(year, month) }
        .flatMapLatest { (year, month) ->
            repository.getBudgetStatus(year, month)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetStatus(
                year = DateUtils.getCurrentYear(),
                month = DateUtils.getCurrentMonth(),
                budgetAmount = 0L,
                spentAmount = 0L,
                remainingAmount = 0L,
                spentPercentage = 0f
            )
        )

    fun setSelectedMonthYear(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // --- SAVE INCOME (Guarded against duplicate calls) ---
    fun addIncome(
        amount: Long,
        source: String,
        description: String,
        date: Long,
        onSuccess: () -> Unit
    ) {
        if (_isProcessing.value) return
        if (amount <= 0) {
            _userMessage.value = "Amount must be greater than zero"
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val now = System.currentTimeMillis()
                val income = IncomeEntity(
                    amount = amount,
                    source = source.trim().ifBlank { "Other" },
                    description = description.trim(),
                    date = date,
                    createdAt = now,
                    updatedAt = now
                )
                repository.insertIncome(income)
                _userMessage.value = "Income added successfully"
                onSuccess()
            } catch (e: Exception) {
                _userMessage.value = "Failed to add income: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // --- UPDATE INCOME ---
    fun updateIncome(
        id: Long,
        amount: Long,
        source: String,
        description: String,
        date: Long,
        createdAt: Long,
        onSuccess: () -> Unit
    ) {
        if (_isProcessing.value) return
        if (amount <= 0) {
            _userMessage.value = "Amount must be greater than zero"
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val updatedIncome = IncomeEntity(
                    id = id,
                    amount = amount,
                    source = source.trim().ifBlank { "Other" },
                    description = description.trim(),
                    date = date,
                    createdAt = createdAt,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateIncome(updatedIncome)
                _userMessage.value = "Income updated successfully"
                onSuccess()
            } catch (e: Exception) {
                _userMessage.value = "Failed to update income: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // --- DELETE INCOME ---
    fun deleteIncome(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteIncomeById(id)
                _userMessage.value = "Income deleted"
            } catch (e: Exception) {
                _userMessage.value = "Failed to delete income: ${e.message}"
            }
        }
    }

    // --- SAVE EXPENSE (Guarded against duplicate calls) ---
    fun addExpense(
        amount: Long,
        category: String,
        description: String,
        date: Long,
        onSuccess: () -> Unit
    ) {
        if (_isProcessing.value) return
        if (amount <= 0) {
            _userMessage.value = "Amount must be greater than zero"
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val now = System.currentTimeMillis()
                val expense = ExpenseEntity(
                    amount = amount,
                    category = category.trim().ifBlank { "Other" },
                    description = description.trim(),
                    date = date,
                    createdAt = now,
                    updatedAt = now
                )
                repository.insertExpense(expense)
                _userMessage.value = "Expense added successfully"
                onSuccess()
            } catch (e: Exception) {
                _userMessage.value = "Failed to add expense: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // --- UPDATE EXPENSE ---
    fun updateExpense(
        id: Long,
        amount: Long,
        category: String,
        description: String,
        date: Long,
        createdAt: Long,
        onSuccess: () -> Unit
    ) {
        if (_isProcessing.value) return
        if (amount <= 0) {
            _userMessage.value = "Amount must be greater than zero"
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val updatedExpense = ExpenseEntity(
                    id = id,
                    amount = amount,
                    category = category.trim().ifBlank { "Other" },
                    description = description.trim(),
                    date = date,
                    createdAt = createdAt,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateExpense(updatedExpense)
                _userMessage.value = "Expense updated successfully"
                onSuccess()
            } catch (e: Exception) {
                _userMessage.value = "Failed to update expense: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // --- DELETE EXPENSE ---
    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteExpenseById(id)
                _userMessage.value = "Expense deleted"
            } catch (e: Exception) {
                _userMessage.value = "Failed to delete expense: ${e.message}"
            }
        }
    }

    // --- SET BUDGET ---
    fun setBudget(year: Int, month: Int, amount: Long, onSuccess: () -> Unit) {
        if (_isProcessing.value) return
        if (amount < 0) {
            _userMessage.value = "Budget cannot be negative"
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                repository.setBudget(year, month, amount)
                _userMessage.value = "Monthly budget updated"
                onSuccess()
            } catch (e: Exception) {
                _userMessage.value = "Failed to set budget: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // --- CLEAR ALL DATA ---
    fun clearAllData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.clearAllData()
                _userMessage.value = "All data cleared successfully"
                onSuccess()
            } catch (e: Exception) {
                _userMessage.value = "Failed to clear data: ${e.message}"
            }
        }
    }

    // --- CSV EXPORT GENERATOR ---
    fun exportToCsv(): String {
        val transactions = allTransactions.value
        val sb = StringBuilder()
        sb.append("ID,Type,Amount (PKR),Category/Source,Description,Date,Formatted Date\n")
        transactions.forEach { item ->
            val dateStr = DateUtils.formatDisplayDate(item.date)
            val descSafe = "\"${item.description.replace("\"", "\"\"")}\""
            val catSafe = "\"${item.categoryOrSource.replace("\"", "\"\"")}\""
            sb.append("${item.id},${item.type.name},${item.amount},$catSafe,$descSafe,${item.date},$dateStr\n")
        }
        return sb.toString()
    }

    suspend fun getIncomeById(id: Long): IncomeEntity? {
        return repository.getIncomeById(id)
    }

    suspend fun getExpenseById(id: Long): ExpenseEntity? {
        return repository.getExpenseById(id)
    }
}

class ExpenseTrackViewModelFactory(
    private val repository: ExpenseTrackRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseTrackViewModel::class.java)) {
            return ExpenseTrackViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
