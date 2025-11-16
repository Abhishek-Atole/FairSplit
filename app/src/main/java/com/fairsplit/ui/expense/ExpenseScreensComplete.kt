package com.fairsplit.ui.expense

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fairsplit.domain.model.ExpenseCategory
import com.fairsplit.domain.model.PersonalExpense
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Complete Expense Tracker UI
 * Includes: Entry Form, List View, Filtering, Search, Receipt Upload
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreenComplete(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedReceiptUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    // Receipt picker launcher
    val receiptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedReceiptUri = uri
    }

    // Show error snackbar
    LaunchedEffect(uiState) {
        when (uiState) {
            is ExpenseUiState.Success -> {
                // Success handled, could navigate back
            }
            is ExpenseUiState.Error -> {
                // Error shown in UI
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Message
            if (uiState is ExpenseUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, "Error", tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = (uiState as ExpenseUiState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, "Dismiss")
                        }
                    }
                }
            }

            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (₹)") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, "Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is ExpenseUiState.Loading
            )

            // Category Selector
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ExpenseCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.displayName) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (category) {
                                    ExpenseCategory.FOOD -> Icons.Default.Restaurant
                                    ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsCar
                                    ExpenseCategory.SHOPPING -> Icons.Default.ShoppingCart
                                    ExpenseCategory.BILLS -> Icons.Default.Receipt
                                    ExpenseCategory.ENTERTAINMENT -> Icons.Default.Movie
                                    ExpenseCategory.OTHER -> Icons.Default.Category
                                },
                                contentDescription = category.displayName
                            )
                        }
                    )
                }
            }

            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Description, "Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                enabled = uiState !is ExpenseUiState.Loading
            )

            // Date Picker
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, "Date")
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Date", style = MaterialTheme.typography.labelSmall)
                        Text(
                            selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Receipt Upload
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { receiptLauncher.launch("image/*") },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CameraAlt, "Receipt")
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Receipt", style = MaterialTheme.typography.labelSmall)
                        Text(
                            selectedReceiptUri?.lastPathSegment ?: "No receipt attached",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (selectedReceiptUri != null) {
                        IconButton(onClick = { selectedReceiptUri = null }) {
                            Icon(Icons.Default.Close, "Remove receipt")
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0) {
                        viewModel.addExpense(
                            amount = amountValue,
                            category = selectedCategory,
                            description = description,
                            date = selectedDate,
                            receiptUri = selectedReceiptUri
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotBlank() && description.isNotBlank() && uiState !is ExpenseUiState.Loading
            ) {
                if (uiState is ExpenseUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Save Expense")
            }
        }
    }

    // Date Picker Dialog (simplified - in real app use DatePickerDialog)
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Date") },
            text = { Text("Date picker implementation pending (use system picker)") },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreenComplete(
    viewModel: ExpenseViewModel,
    onNavigateToEntry: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val expenseList by viewModel.expenseList.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showFilterMenu by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                actions = {
                    // Search toggle
                    IconButton(onClick = { /* Toggle search */ }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                    // Filter menu
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                    
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                viewModel.filterByCategory(null)
                                showFilterMenu = false
                            }
                        )
                        ExpenseCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName) },
                                onClick = {
                                    viewModel.filterByCategory(category)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToEntry) {
                Icon(Icons.Default.Add, "Add Expense")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    viewModel.searchExpenses(it)
                },
                label = { Text("Search expenses...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = {
                            searchText = ""
                            viewModel.searchExpenses("")
                        }) {
                            Icon(Icons.Default.Close, "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // Active Filter Chip
            if (selectedCategory != null) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter: ", style = MaterialTheme.typography.labelSmall)
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.filterByCategory(null) },
                        label = { Text(selectedCategory!!.displayName) },
                        trailingIcon = { Icon(Icons.Default.Close, "Clear filter") }
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            when (uiState) {
                is ExpenseUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ExpenseUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Error,
                                "Error",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = (uiState as ExpenseUiState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                else -> {
                    if (expenseList.isEmpty()) {
                        // Empty State
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Receipt,
                                    "No expenses",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "No expenses yet",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Tap + to add your first expense",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        // Expense List
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(expenseList) { expense ->
                                ExpenseCard(
                                    expense = expense,
                                    onDelete = { viewModel.deleteExpense(expense.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseCard(
    expense: PersonalExpense,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (expense.category) {
                            ExpenseCategory.FOOD -> Icons.Default.Restaurant
                            ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsCar
                            ExpenseCategory.SHOPPING -> Icons.Default.ShoppingCart
                            ExpenseCategory.BILLS -> Icons.Default.Receipt
                            ExpenseCategory.ENTERTAINMENT -> Icons.Default.Movie
                            ExpenseCategory.OTHER -> Icons.Default.Category
                        },
                        contentDescription = expense.category.displayName,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Expense Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    Text(
                        text = expense.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(" • ", color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = expense.date.format(DateTimeFormatter.ofPattern("dd MMM")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%.2f", expense.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                if (expense.receiptUrl != null) {
                    Icon(
                        Icons.Default.AttachFile,
                        "Has receipt",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Delete Button
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to delete this expense? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
