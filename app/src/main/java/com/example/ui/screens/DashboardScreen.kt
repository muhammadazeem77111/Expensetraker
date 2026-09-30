package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.components.IncomeExpenseRow
import com.example.ui.components.TotalBalanceCard
import com.example.ui.components.TransactionCard
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeEmerald
import com.example.ui.viewmodel.ExpenseTrackViewModel
import com.example.util.DateUtils

@Composable
fun DashboardScreen(
    viewModel: ExpenseTrackViewModel,
    onNavigateToAddIncome: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditIncome: (Long) -> Unit,
    onNavigateToEditExpense: (Long) -> Unit,
    onNavigateToTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overallSummary by viewModel.overallSummary.collectAsStateWithLifecycle()
    val currentMonthSummary by viewModel.currentMonthSummary.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()

    var itemToDelete by remember { mutableStateOf<TransactionItem?>(null) }

    val currentMonthName = DateUtils.formatMonthYear(
        DateUtils.getCurrentYear(),
        DateUtils.getCurrentMonth()
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top App Bar Greeting / Month Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ExpenseTrack",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Financial clarity & strict zero-drift accounting",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("current_month_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentMonthName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Hero Total Balance Card (All-time derived balance)
        item {
            TotalBalanceCard(
                balance = overallSummary.balance,
                subtitle = "Total Balance",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Income & Expense Breakdown Row
        item {
            IncomeExpenseRow(
                totalIncome = overallSummary.totalIncome,
                totalExpenses = overallSummary.totalExpenses,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Quick Action Buttons (Add Income & Add Expense)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToAddIncome,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IncomeEmerald
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dashboard_add_income_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Income",
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onNavigateToAddExpense,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExpenseCoral
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dashboard_add_expense_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Expense",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Current Month Mini Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("current_month_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "This Month ($currentMonthName)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Income: Rs. ${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(currentMonthSummary.totalIncome)}  •  Spent: Rs. ${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(currentMonthSummary.totalExpenses)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Net: Rs. ${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(currentMonthSummary.balance)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (currentMonthSummary.balance >= 0) IncomeEmerald else ExpenseCoral,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Recent Transactions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (recentTransactions.isNotEmpty()) {
                    TextButton(
                        onClick = onNavigateToTransactions,
                        modifier = Modifier.testTag("view_all_transactions_button")
                    ) {
                        Text("View All")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Recent Transactions List or Empty State
        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("empty_transactions_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Transactions Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your first income or expense above to start tracking. Your balance will be calculated strictly from database entries.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(
                items = recentTransactions,
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

        item {
            Spacer(modifier = Modifier.height(50.dp))
        }
    }

    // Confirmation dialog before deleting transaction
    itemToDelete?.let { item ->
        DeleteConfirmDialog(
            title = "Delete ${if (item.type == TransactionType.INCOME) "Income" else "Expense"}",
            message = "Are you sure you want to delete this ${item.categoryOrSource} record for Rs. ${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(item.amount)}? The database will recalculate your totals automatically.",
            onConfirm = {
                if (item.type == TransactionType.INCOME) {
                    viewModel.deleteIncome(item.id)
                } else {
                    viewModel.deleteExpense(item.id)
                }
                itemToDelete = null
            },
            onDismiss = {
                itemToDelete = null
            }
        )
    }
}
