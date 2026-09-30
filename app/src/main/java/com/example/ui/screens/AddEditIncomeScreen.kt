package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.IncomeEmerald
import com.example.ui.viewmodel.ExpenseTrackViewModel
import com.example.util.DateUtils
import java.util.Calendar

private val DefaultIncomeSources = listOf(
    "Salary", "Business", "Freelance", "Gift", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditIncomeScreen(
    incomeId: Long? = null,
    viewModel: ExpenseTrackViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var amountText by remember { mutableStateOf("") }
    var selectedSource by remember { mutableStateOf("Salary") }
    var customSource by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var originalCreatedAt by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var isLoaded by remember { mutableStateOf(false) }

    // Intercept back gesture
    BackHandler {
        onNavigateBack()
    }

    // Load existing record if in Edit mode
    LaunchedEffect(incomeId) {
        if (incomeId != null && incomeId > 0 && !isLoaded) {
            val record = viewModel.getIncomeById(incomeId)
            if (record != null) {
                amountText = record.amount.toString()
                if (DefaultIncomeSources.contains(record.source)) {
                    selectedSource = record.source
                } else {
                    selectedSource = "Other"
                    customSource = record.source
                }
                descriptionText = record.description
                selectedDateMillis = record.date
                originalCreatedAt = record.createdAt
            }
            isLoaded = true
        }
    }

    // DatePicker dialog launcher
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDateMillis = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val isEditing = incomeId != null && incomeId > 0
    val screenTitle = if (isEditing) "Edit Income" else "Add Income"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button_income")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier.testTag("add_edit_income_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Income Amount (PKR)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = {
                                amountText = it.filter { ch -> ch.isDigit() }
                                amountError = null
                            },
                            placeholder = { Text("0") },
                            prefix = {
                                Text(
                                    text = "Rs. ",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = IncomeEmerald
                                    )
                                )
                            },
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = amountError != null,
                            supportingText = {
                                if (amountError != null) {
                                    Text(amountError!!, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("Enter positive whole PKR rupees")
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("income_amount_input")
                        )
                    }
                }
            }

            // Source Chips
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Income Source",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DefaultIncomeSources.forEach { source ->
                                val isSelected = selectedSource == source
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSource = source },
                                    label = { Text(source) },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IncomeEmerald.copy(alpha = 0.18f),
                                        selectedLabelColor = IncomeEmerald
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("source_chip_$source")
                                )
                            }
                        }

                        if (selectedSource == "Other") {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = customSource,
                                onValueChange = { customSource = it },
                                placeholder = { Text("Specify source name...") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_source_input")
                            )
                        }
                    }
                }
            }

            // Description / Note Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Description / Note (Optional)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = descriptionText,
                            onValueChange = { descriptionText = it },
                            placeholder = { Text("e.g. Monthly salary from tech company") },
                            maxLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("income_description_input")
                        )
                    }
                }
            }

            // Date Selection Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Transaction Date",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = DateUtils.formatDisplayDate(selectedDateMillis),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.testTag("income_date_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Pick Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                }
            }

            // Save Action Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val amount = amountText.toLongOrNull()
                        if (amount == null || amount <= 0) {
                            amountError = "Please enter a valid amount greater than zero"
                            return@Button
                        }

                        val sourceFinal = if (selectedSource == "Other" && customSource.isNotBlank()) {
                            customSource.trim()
                        } else {
                            selectedSource
                        }

                        if (isEditing) {
                            viewModel.updateIncome(
                                id = incomeId!!,
                                amount = amount,
                                source = sourceFinal,
                                description = descriptionText,
                                date = selectedDateMillis,
                                createdAt = originalCreatedAt,
                                onSuccess = onNavigateBack
                            )
                        } else {
                            viewModel.addIncome(
                                amount = amount,
                                source = sourceFinal,
                                description = descriptionText,
                                date = selectedDateMillis,
                                onSuccess = onNavigateBack
                            )
                        }
                    },
                    enabled = !isProcessing && amountText.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IncomeEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_income_button")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            text = if (isEditing) "Update Income" else "Save Income",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
