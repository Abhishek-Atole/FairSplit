package com.fairsplit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fairsplit.domain.model.ExpenseCategory
import com.fairsplit.domain.model.PersonalExpense
import com.fairsplit.ui.expense.ExpenseUiState
import com.fairsplit.ui.expense.ExpenseViewModel
import com.fairsplit.ui.theme.*
import com.fairsplit.domain.repository.ExpenseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Create ViewModel with simplified repository
    val viewModel = remember {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        
        val repository = object : ExpenseRepository {
            override suspend fun addExpense(expense: PersonalExpense): com.fairsplit.domain.util.Result<String> {
                return com.fairsplit.domain.util.Result.Success("")
            }
            
            override fun getExpensesByMonth(month: Int, year: Int) = kotlinx.coroutines.flow.flowOf<List<PersonalExpense>>(emptyList())
            override fun getExpensesByCategory(category: ExpenseCategory) = kotlinx.coroutines.flow.flowOf<List<PersonalExpense>>(emptyList())
            override fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate) = kotlinx.coroutines.flow.flowOf<List<PersonalExpense>>(emptyList())
            override suspend fun getTotalSpent(financeId: String) = com.fairsplit.domain.util.Result.Success(0.0)
            override suspend fun getTotalSpent(userId: String, month: Int, year: Int) = com.fairsplit.domain.util.Result.Success(0.0)
            override suspend fun updateExpense(expense: PersonalExpense) = com.fairsplit.domain.util.Result.Success(Unit)
            override suspend fun deleteExpense(expenseId: String) = com.fairsplit.domain.util.Result.Success(Unit)
            override suspend fun uploadReceipt(expenseId: String, imageUri: android.net.Uri) = com.fairsplit.domain.util.Result.Success("")
        }
        
        ExpenseViewModel(repository, auth)
    }
    
    val expenses by viewModel.expenseList.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedDateFilter by remember { mutableStateOf("This Month") }
    var selectedCategoryFilter by remember { mutableStateOf<ExpenseCategory?>(null) }
    var selectedSortOption by remember { mutableStateOf("Newest") }
    
    // Filter and sort expenses
    val filteredExpenses = remember(expenses, searchQuery, selectedCategoryFilter, selectedSortOption) {
        var result = expenses
        
        // Apply search filter
        if (searchQuery.isNotEmpty()) {
            result = result.filter { expense ->
                expense.description.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Apply category filter
        if (selectedCategoryFilter != null) {
            result = result.filter { expense -> expense.category == selectedCategoryFilter }
        }
        
        // Apply sorting
        result = when (selectedSortOption) {
            "Newest" -> result.sortedByDescending { expense -> expense.date }
            "Oldest" -> result.sortedBy { expense -> expense.date }
            "Highest Amount" -> result.sortedByDescending { expense -> expense.amount }
            "Lowest Amount" -> result.sortedBy { expense -> expense.amount }
            else -> result
        }
        
        result
    }
    
    // Group expenses by date sections
    val groupedExpenses = remember(filteredExpenses) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        filteredExpenses.groupBy { expense ->
            when {
                expense.date == today -> "Today"
                expense.date == yesterday -> "Yesterday"
                expense.date.isAfter(today.minusDays(7)) -> "This Week"
                expense.date.isAfter(today.minusMonths(1)) -> "This Month"
                else -> expense.date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            }
        }
    }
    
    Scaffold(
        topBar = {
            ExpenseListTopBar(
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_expense") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
            )
            
            // Filter Chips
            FilterChips(
                selectedDateFilter = selectedDateFilter,
                selectedCategoryFilter = selectedCategoryFilter,
                selectedSortOption = selectedSortOption,
                onDateFilterClick = { /* TODO: Show date picker */ },
                onCategoryFilterClick = { /* TODO: Show category picker */ },
                onSortOptionClick = { /* TODO: Show sort picker */ },
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
            )
            
            // Expenses List
            if (filteredExpenses.isEmpty()) {
                EmptyState(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    groupedExpenses.forEach { (section: String, sectionExpenses: List<PersonalExpense>) ->
                        item {
                            SectionHeader(title = section)
                        }
                        
                        items(sectionExpenses, key = { expense -> expense.id }) { expense ->
                            ExpenseItem(
                                expense = expense,
                                onClick = { /* TODO: Navigate to expense details */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseListTopBar(
    onBackClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Text(
                    text = "Personal Expenses",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = Spacing.sm)
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Search by merchant, note...",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        ),
        singleLine = true
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FilterChips(
    selectedDateFilter: String,
    selectedCategoryFilter: ExpenseCategory?,
    selectedSortOption: String,
    onDateFilterClick: () -> Unit,
    onCategoryFilterClick: () -> Unit,
    onSortOptionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        // Date Filter Chip
        FilterChip(
            selected = true,
            onClick = onDateFilterClick,
            label = {
                Text(
                    text = "Date: $selectedDateFilter",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                selectedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        
        // Category Filter Chip
        FilterChip(
            selected = selectedCategoryFilter != null,
            onClick = onCategoryFilterClick,
            label = {
                Text(
                    text = "Category: ${selectedCategoryFilter?.name ?: "All"}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        
        // Sort Filter Chip
        FilterChip(
            selected = false,
            onClick = onSortOptionClick,
            label = {
                Text(
                    text = "Sort: $selectedSortOption",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(vertical = Spacing.xs)
    )
}

@Composable
private fun ExpenseItem(
    expense: PersonalExpense,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember { DecimalFormat("#,##0.00") }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CustomShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(getCategoryColor(expense.category).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(expense.category),
                        contentDescription = null,
                        tint = getCategoryColor(expense.category),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = expense.description,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = expense.date.format(DateTimeFormatter.ofPattern("MMM dd")),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = "-$${currencyFormatter.format(expense.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = getCategoryColor(expense.category).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = expense.category.name.lowercase().capitalize(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = getCategoryColor(expense.category),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Expenses Found",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Spacing.sm))
        
        Text(
            text = "Try adjusting your filters or add a new expense to see it here.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )
    }
}

private fun getCategoryColor(category: ExpenseCategory): Color {
    return when (category) {
        ExpenseCategory.FOOD -> Color(0xFFFF9800)
        ExpenseCategory.TRANSPORT -> Color(0xFF00BCD4)
        ExpenseCategory.SHOPPING -> Color(0xFF9C27B0)
        ExpenseCategory.ENTERTAINMENT -> Color(0xFFE91E63)
        ExpenseCategory.BILLS -> Color(0xFF2196F3)
        ExpenseCategory.OTHER -> Color(0xFF607D8B)
    }
}

private fun getCategoryIcon(category: ExpenseCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsCar
        ExpenseCategory.SHOPPING -> Icons.Default.ShoppingBag
        ExpenseCategory.ENTERTAINMENT -> Icons.Default.Gamepad
        ExpenseCategory.BILLS -> Icons.Default.Receipt
        ExpenseCategory.OTHER -> Icons.Default.MoreHoriz
    }
}
