package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MonthYearPickerDialog
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeEmerald
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.ExpenseTrackViewModel
import com.example.util.DateUtils
import com.example.util.FinanceFormatter
import java.util.Locale

@Composable
fun BudgetScreen(
    viewModel: ExpenseTrackViewModel,
    modifier: Modifier = Modifier
) {
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val budgetStatus by viewModel.budgetStatus.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    var showMonthPicker by remember { mutableStateOf(false) }
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    val formattedMonthYear = DateUtils.formatMonthYear(selectedYear, selectedMonth)

    val progressFraction = if (budgetStatus.budgetAmount > 0) {
        (budgetStatus.spentAmount.toFloat() / budgetStatus.budgetAmount.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val progressColor = when {
        budgetStatus.spentAmount > budgetStatus.budgetAmount && budgetStatus.budgetAmount > 0 -> ExpenseCoral
        progressFraction > 0.8f -> WarningAmber
        else -> MaterialTheme.colorScheme.primary
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("budget_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Budget",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Set spending limits without modifying balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Month Picker Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clickable { showMonthPicker = true }
                        .testTag("budget_month_picker_trigger")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Select Month",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedMonthYear,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Main Budget Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_main_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Budget Target ($formattedMonthYear)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = {
                                budgetInput = if (budgetStatus.budgetAmount > 0) budgetStatus.budgetAmount.toString() else ""
                                inputError = null
                                showSetBudgetDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("set_budget_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (budgetStatus.budgetAmount > 0) "Edit" else "Set")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (budgetStatus.budgetAmount > 0) {
                            FinanceFormatter.formatCurrency(budgetStatus.budgetAmount)
                        } else {
                            "No budget set"
                        },
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 30.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("budget_target_amount")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .testTag("budget_progress_bar"),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Spent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FinanceFormatter.formatCurrency(budgetStatus.spentAmount),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ExpenseCoral,
                                modifier = Modifier.testTag("budget_spent_amount")
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Remaining",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FinanceFormatter.formatCurrency(budgetStatus.remainingAmount),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (budgetStatus.remainingAmount >= 0) IncomeEmerald else ExpenseCoral,
                                modifier = Modifier.testTag("budget_remaining_amount")
                            )
                        }
                    }

                    if (budgetStatus.budgetAmount > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        val percentageText = String.format(Locale.US, "%.1f%%", budgetStatus.spentPercentage)
                        val isOverBudget = budgetStatus.spentAmount > budgetStatus.budgetAmount

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isOverBudget) ExpenseCoral.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOverBudget) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isOverBudget) ExpenseCoral else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isOverBudget) {
                                        "Over budget! You have spent $percentageText of your planned limit."
                                    } else {
                                        "On track: You have utilized $percentageText of your monthly budget."
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = if (isOverBudget) ExpenseCoral else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Strict Financial Integrity Notice
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_notice_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Reference Only Guarantee",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "A monthly budget is strictly an informational target. It never automatically creates an expense, never deducts money, and has zero impact on your actual account balance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Set Budget Dialog
    if (showSetBudgetDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isProcessing) showSetBudgetDialog = false
            },
            title = {
                Text(
                    text = "Set Budget for $formattedMonthYear",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your planned spending target in Pakistani Rupees (PKR):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = {
                            budgetInput = it.filter { ch -> ch.isDigit() }
                            inputError = null
                        },
                        label = { Text("Budget Amount (PKR)") },
                        prefix = { Text("Rs. ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = inputError != null,
                        supportingText = {
                            if (inputError != null) {
                                Text(inputError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_amount_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = budgetInput.toLongOrNull()
                        if (amount == null || amount < 0) {
                            inputError = "Please enter a valid amount"
                        } else {
                            viewModel.setBudget(selectedYear, selectedMonth, amount) {
                                showSetBudgetDialog = false
                            }
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier.testTag("confirm_set_budget_button")
                ) {
                    Text("Save Budget")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSetBudgetDialog = false },
                    enabled = !isProcessing,
                    modifier = Modifier.testTag("cancel_set_budget_button")
                ) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.testTag("set_budget_dialog")
        )
    }

    // Month & Year Picker Dialog
    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentYear = selectedYear,
            currentMonth = selectedMonth,
            onSelect = { year, month ->
                viewModel.setSelectedMonthYear(year, month)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}
