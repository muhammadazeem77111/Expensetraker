package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.TransactionItem
import com.example.model.TransactionType
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.MonthYearPickerDialog
import com.example.ui.components.TransactionCard
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeEmerald
import com.example.ui.viewmodel.ExpenseTrackViewModel
import com.example.util.DateUtils

@Composable
fun TransactionsScreen(
    viewModel: ExpenseTrackViewModel,
    onNavigateToEditIncome: (Long) -> Unit,
    onNavigateToEditExpense: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val filterMonth by viewModel.filterMonth.collectAsStateWithLifecycle()
    val filterYear by viewModel.filterYear.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<TransactionItem?>(null) }

    val hasActiveFilters = searchQuery.isNotBlank() || filterType != null || filterMonth != null || filterYear != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("transactions_screen")
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "All Transactions",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Search, filter, edit, or delete any recorded transaction",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_transactions_input"),
                placeholder = { Text("Search by note, category, or amount...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Horizontal Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Filter: ALL
                FilterChip(
                    selected = filterType == null,
                    onClick = { viewModel.filterType.value = null },
                    label = { Text("All") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_chip_all")
                )

                // Type Filter: Income
                FilterChip(
                    selected = filterType == TransactionType.INCOME,
                    onClick = {
                        viewModel.filterType.value =
                            if (filterType == TransactionType.INCOME) null else TransactionType.INCOME
                    },
                    label = { Text("Income") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IncomeEmerald.copy(alpha = 0.2f),
                        selectedLabelColor = IncomeEmerald
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_chip_income")
                )

                // Type Filter: Expense
                FilterChip(
                    selected = filterType == TransactionType.EXPENSE,
                    onClick = {
                        viewModel.filterType.value =
                            if (filterType == TransactionType.EXPENSE) null else TransactionType.EXPENSE
                    },
                    label = { Text("Expense") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExpenseCoral.copy(alpha = 0.2f),
                        selectedLabelColor = ExpenseCoral
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_chip_expense")
                )

                // Date Filter Button
                val dateLabel = if (filterMonth != null && filterYear != null) {
                    "${DateUtils.getMonthName(filterMonth!!)} $filterYear"
                } else {
                    "Select Month"
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (filterMonth != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { showDatePicker = true }
                        .testTag("filter_date_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (filterMonth != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = if (filterMonth != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Clear Filters button
                if (hasActiveFilters) {
                    TextButton(
                        onClick = {
                            viewModel.searchQuery.value = ""
                            viewModel.filterType.value = null
                            viewModel.filterCategory.value = null
                            viewModel.filterMonth.value = null
                            viewModel.filterYear.value = null
                        },
                        modifier = Modifier.testTag("clear_filters_button")
                    ) {
                        Text("Reset", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Result count text
            Text(
                text = "Found ${filteredTransactions.size} transactions",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Transactions List or Empty State
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("no_matching_transactions_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (hasActiveFilters) "No transactions match filters" else "No transactions saved",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hasActiveFilters) "Try adjusting or clearing your search or date filters." else "Add income or expense to see them recorded here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 64.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filteredTransactions,
                    key = { "${it.type}_${it.id}" }
                ) { transaction ->
                    TransactionCard(
                        item = transaction,
                        onEdit = {
                            if (transaction.type == TransactionType.INCOME) {
                                onNavigateToEditIncome(transaction.id)
                            } else {
                                onNavigateToEditExpense(transaction.id)
                            }
                        },
                        onDelete = {
                            itemToDelete = transaction
                        }
                    )
                }
            }
        }
    }

    // Month & Year Picker Dialog for Filter
    if (showDatePicker) {
        MonthYearPickerDialog(
            currentYear = filterYear ?: DateUtils.getCurrentYear(),
            currentMonth = filterMonth ?: DateUtils.getCurrentMonth(),
            onSelect = { year, month ->
                viewModel.filterYear.value = year
                viewModel.filterMonth.value = month
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { item ->
        DeleteConfirmDialog(
            title = "Delete ${if (item.type == TransactionType.INCOME) "Income" else "Expense"}",
            message = "Are you sure you want to delete this ${item.categoryOrSource} record for Rs. ${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(item.amount)}? The database balance will recalculate automatically.",
            onConfirm = {
                if (item.type == TransactionType.INCOME) {
                    viewModel.deleteIncome(item.id)
                } else {
                    viewModel.deleteExpense(item.id)
                }
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}
