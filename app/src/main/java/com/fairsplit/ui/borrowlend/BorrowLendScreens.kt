package com.fairsplit.ui.borrowlend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.fairsplit.domain.model.BorrowLendTransaction
import com.fairsplit.domain.model.TransactionStatus
import com.fairsplit.domain.model.TransactionType
import com.fairsplit.domain.repository.BorrowLendRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * F22: Borrow/Lend Log - ViewModel + Complete UI
 */

class BorrowLendViewModel(
    private val repository: BorrowLendRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<BorrowLendUiState>(BorrowLendUiState.Idle)
    val uiState: StateFlow<BorrowLendUiState> = _uiState.asStateFlow()

    private val _transactions = MutableStateFlow<List<BorrowLendTransaction>>(emptyList())
    val transactions: StateFlow<List<BorrowLendTransaction>> = _transactions.asStateFlow()

    private val _totalBorrowed = MutableStateFlow(0.0)
    val totalBorrowed: StateFlow<Double> = _totalBorrowed.asStateFlow()

    private val _totalLent = MutableStateFlow(0.0)
    val totalLent: StateFlow<Double> = _totalLent.asStateFlow()

    private val _filterType = MutableStateFlow<TransactionType?>(null)
    val filterType: StateFlow<TransactionType?> = _filterType.asStateFlow()

    init {
        loadAllTransactions()
        loadTotals()
    }

    fun addTransaction(
        type: TransactionType,
        personName: String,
        amount: Double,
        description: String,
        dueDate: Instant? = null
    ) {
        viewModelScope.launch {
            _uiState.value = BorrowLendUiState.Loading
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.value = BorrowLendUiState.Error("User not authenticated")
                return@launch
            }

            val transaction = BorrowLendTransaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = type,
                personName = personName,
                amount = amount,
                description = description,
                date = Instant.now(),
                status = TransactionStatus.PENDING,
                dueDate = dueDate,
                settledDate = null
            )

            when (val result = repository.addTransaction(transaction)) {
                is Result.Success -> {
                    _uiState.value = BorrowLendUiState.Success("Transaction added successfully")
                    loadAllTransactions()
                    loadTotals()
                }
                is Result.Error -> {
                    _uiState.value = BorrowLendUiState.Error(result.message)
                }
            }
        }
    }

    fun loadAllTransactions() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getAllTransactions(userId).collect { list ->
                _transactions.value = list
            }
        }
    }

    fun filterByType(type: TransactionType?) {
        _filterType.value = type
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            if (type == null) {
                repository.getAllTransactions(userId).collect { list ->
                    _transactions.value = list
                }
            } else {
                repository.getTransactionsByType(userId, type).collect { list ->
                    _transactions.value = list
                }
            }
        }
    }

    fun markAsSettled(transactionId: String) {
        viewModelScope.launch {
            _uiState.value = BorrowLendUiState.Loading
            when (val result = repository.markAsSettled(transactionId)) {
                is Result.Success -> {
                    _uiState.value = BorrowLendUiState.Success("Transaction marked as settled")
                    loadAllTransactions()
                    loadTotals()
                }
                is Result.Error -> {
                    _uiState.value = BorrowLendUiState.Error(result.message)
                }
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.value = BorrowLendUiState.Loading
            when (val result = repository.deleteTransaction(transactionId)) {
                is Result.Success -> {
                    _uiState.value = BorrowLendUiState.Success("Transaction deleted")
                    loadAllTransactions()
                    loadTotals()
                }
                is Result.Error -> {
                    _uiState.value = BorrowLendUiState.Error(result.message)
                }
            }
        }
    }

    private fun loadTotals() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            when (val borrowedResult = repository.getTotalPending(userId, TransactionType.BORROWED)) {
                is Result.Success -> _totalBorrowed.value = borrowedResult.data
                is Result.Error -> {}
            }
            when (val lentResult = repository.getTotalPending(userId, TransactionType.LENT)) {
                is Result.Success -> _totalLent.value = lentResult.data
                is Result.Error -> {}
            }
        }
    }

    fun resetUiState() {
        _uiState.value = BorrowLendUiState.Idle
    }
}

sealed class BorrowLendUiState {
    object Idle : BorrowLendUiState()
    object Loading : BorrowLendUiState()
    data class Success(val message: String) : BorrowLendUiState()
    data class Error(val message: String) : BorrowLendUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowLendDashboardScreen(navController: NavController) {
    // Mock ViewModel for now (TODO: Inject via Hilt)
    val transactions = remember { mutableStateOf(emptyList<BorrowLendTransaction>()) }
    val totalBorrowed = remember { mutableStateOf(0.0) }
    val totalLent = remember { mutableStateOf(0.0) }
    val selectedFilter = remember { mutableStateOf<TransactionType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Borrow/Lend Log") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("borrowlend_entry") }) {
                Icon(Icons.Default.Add, "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "You Owe",
                    amount = totalBorrowed.value,
                    icon = Icons.Default.ArrowDownward,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "You'll Get",
                    amount = totalLent.value,
                    icon = Icons.Default.ArrowUpward,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter.value == null,
                    onClick = { selectedFilter.value = null },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedFilter.value == TransactionType.BORROWED,
                    onClick = { selectedFilter.value = TransactionType.BORROWED },
                    label = { Text("Borrowed") }
                )
                FilterChip(
                    selected = selectedFilter.value == TransactionType.LENT,
                    onClick = { selectedFilter.value = TransactionType.LENT },
                    label = { Text("Lent") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transactions List
            if (transactions.value.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No transactions yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "Tap + to add a transaction",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions.value) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onSettle = { /* TODO */ },
                            onDelete = { /* TODO */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₹%.2f".format(amount),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionCard(
    transaction: BorrowLendTransaction,
    onSettle: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy")
            .withZone(ZoneId.systemDefault())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on type
            Icon(
                imageVector = if (transaction.type == TransactionType.BORROWED)
                    Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = if (transaction.type == TransactionType.BORROWED)
                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.personName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dateFormatter.format(transaction.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (transaction.status == TransactionStatus.SETTLED) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Settled", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount and actions
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹%.2f".format(transaction.amount),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (transaction.type == TransactionType.BORROWED)
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                if (transaction.status == TransactionStatus.PENDING) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onSettle, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Check, "Settle", Modifier.size(20.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Delete", Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowLendEntryScreen(navController: NavController) {
    var transactionType by remember { mutableStateOf(TransactionType.BORROWED) }
    var personName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Transaction Type Selector
            Text(
                text = "Transaction Type",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = transactionType == TransactionType.BORROWED,
                    onClick = { transactionType = TransactionType.BORROWED },
                    label = { Text("I Borrowed") },
                    leadingIcon = { Icon(Icons.Default.ArrowDownward, null, Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = transactionType == TransactionType.LENT,
                    onClick = { transactionType = TransactionType.LENT },
                    label = { Text("I Lent") },
                    leadingIcon = { Icon(Icons.Default.ArrowUpward, null, Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Person Name
            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                label = { Text("Person Name") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("₹") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            if (showError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    when {
                        personName.isBlank() -> {
                            showError = true
                            errorMessage = "Please enter person name"
                        }
                        amount.isBlank() || amount.toDoubleOrNull() == null -> {
                            showError = true
                            errorMessage = "Please enter valid amount"
                        }
                        description.isBlank() -> {
                            showError = true
                            errorMessage = "Please enter description"
                        }
                        else -> {
                            showError = false
                            // TODO: Save transaction via ViewModel
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, "Save", Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Transaction")
            }
        }
    }
}
