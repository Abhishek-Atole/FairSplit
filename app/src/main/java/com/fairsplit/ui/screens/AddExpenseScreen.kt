package com.fairsplit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fairsplit.domain.model.ExpenseCategory
import com.fairsplit.ui.expense.AddExpenseUiState
import com.fairsplit.ui.expense.AddExpenseViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    // Keyboard controller for managing keyboard visibility
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()
    
    // Local state
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddExpenseUiState.Success -> {
                keyboardController?.hide()
                snackbarHostState.showSnackbar(
                    message = "✓ Expense saved: ₹${state.expense.amount}",
                    duration = SnackbarDuration.Short
                )
                amount = ""
                description = ""
                selectedCategory = ExpenseCategory.FOOD
                selectedDate = LocalDate.now()
                viewModel.resetState()
                kotlinx.coroutines.delay(1000)
                navController.popBackStack()
            }
            is AddExpenseUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = "Error: ${state.message}",
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Add Expense",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        keyboardController?.hide()
                        navController.popBackStack() 
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Amount Card - Modern Design
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "AMOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = if (amount.isEmpty()) "0.00" else formatDisplayAmount(amount),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 48.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    placeholder = { Text("What did you spend on?") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    )
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Category & Date Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Card
                    Card(
                        onClick = { showCategorySheet = true },
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(selectedCategory),
                                contentDescription = null,
                                tint = getCategoryColor(selectedCategory),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedCategory.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Date Card
                    Card(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM")),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Extra space for keyboard
                Spacer(modifier = Modifier.height(340.dp))
            }
            
            // Numeric Keypad - Modern Fixed Bottom Design
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 16.dp
            ) {
                Column {
                    Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )
                    
                    ModernNumericKeypad(
                        currentValue = amount,
                        onValueChange = { amount = it },
                        onClear = { amount = "" },
                        onSave = {
                            keyboardController?.hide()
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue != null && amountValue > 0 && description.isNotBlank()) {
                                viewModel.addExpense(
                                    amount = amountValue,
                                    category = selectedCategory,
                                    description = description,
                                    date = selectedDate
                                )
                            }
                        },
                        canSave = amount.isNotEmpty() && description.isNotBlank(),
                        isLoading = uiState is AddExpenseUiState.Loading
                    )
                }
            }
        }
        
        // Category Bottom Sheet
        if (showCategorySheet) {
            @OptIn(ExperimentalMaterial3Api::class)
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Select Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                    
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ExpenseCategory.values()) { category ->
                            Card(
                                onClick = {
                                    selectedCategory = category
                                    showCategorySheet = false
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedCategory == category) {
                                        getCategoryColor(category).copy(alpha = 0.15f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = if (selectedCategory == category) {
                                    BorderStroke(2.dp, getCategoryColor(category))
                                } else null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(category),
                                        contentDescription = null,
                                        tint = getCategoryColor(category),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Date Picker Dialog
        if (showDatePicker) {
            @OptIn(ExperimentalMaterial3Api::class)
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    selectedDate = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
            }
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

// Helper function to format amount display
@Composable
private fun formatDisplayAmount(amount: String): String {
    return try {
        val value = amount.toDoubleOrNull() ?: 0.0
        if (amount.contains(".")) {
            amount
        } else if (value == 0.0) {
            "0.00"
        } else {
            amount
        }
    } catch (e: Exception) {
        "0.00"
    }
}

// Modern Numeric Keypad Component
@Composable
private fun ModernNumericKeypad(
    currentValue: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        // Row 1: 1, 2, 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton("1", Modifier.weight(1f)) { onValueChange(currentValue + "1") }
            KeypadButton("2", Modifier.weight(1f)) { onValueChange(currentValue + "2") }
            KeypadButton("3", Modifier.weight(1f)) { onValueChange(currentValue + "3") }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Row 2: 4, 5, 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton("4", Modifier.weight(1f)) { onValueChange(currentValue + "4") }
            KeypadButton("5", Modifier.weight(1f)) { onValueChange(currentValue + "5") }
            KeypadButton("6", Modifier.weight(1f)) { onValueChange(currentValue + "6") }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Row 3: 7, 8, 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton("7", Modifier.weight(1f)) { onValueChange(currentValue + "7") }
            KeypadButton("8", Modifier.weight(1f)) { onValueChange(currentValue + "8") }
            KeypadButton("9", Modifier.weight(1f)) { onValueChange(currentValue + "9") }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Row 4: ., 0, ⌫
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(".", Modifier.weight(1f)) { 
                if (!currentValue.contains(".")) {
                    onValueChange(if (currentValue.isEmpty()) "0." else currentValue + ".")
                }
            }
            KeypadButton("0", Modifier.weight(1f)) { onValueChange(currentValue + "0") }
            KeypadIconButton(
                icon = Icons.Default.ArrowBack,
                modifier = Modifier.weight(1f)
            ) {
                if (currentValue.isNotEmpty()) {
                    onValueChange(currentValue.dropLast(1))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = canSave && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(14.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Save Expense",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    @OptIn(ExperimentalMaterial3Api::class)
    Card(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun KeypadIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    @OptIn(ExperimentalMaterial3Api::class)
    Card(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseTopBar(onClose: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Add Expense",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            // Empty spacer to center the title
            Spacer(modifier = Modifier.width(48.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// Helper functions for category icons and colors
@Composable
private fun getCategoryIcon(category: ExpenseCategory): ImageVector {
    return when (category) {
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsCar
        ExpenseCategory.SHOPPING -> Icons.Default.ShoppingCart
        ExpenseCategory.ENTERTAINMENT -> Icons.Default.Movie
        ExpenseCategory.BILLS -> Icons.Default.Receipt
        ExpenseCategory.OTHER -> Icons.Default.MoreHoriz
    }
}

@Composable
private fun getCategoryColor(category: ExpenseCategory): Color {
    return when (category) {
        ExpenseCategory.FOOD -> Color(0xFFFF6B6B)
        ExpenseCategory.TRANSPORT -> Color(0xFF4ECDC4)
        ExpenseCategory.SHOPPING -> Color(0xFFFFBE0B)
        ExpenseCategory.ENTERTAINMENT -> Color(0xFFB15BFF)
        ExpenseCategory.BILLS -> Color(0xFFFF8C42)
        ExpenseCategory.OTHER -> Color(0xFF9E9E9E)
    }
}
