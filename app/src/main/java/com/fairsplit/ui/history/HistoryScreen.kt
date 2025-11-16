package com.fairsplit.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fairsplit.domain.model.ArchivedMonth
import com.fairsplit.domain.model.ArchiveStats
import com.fairsplit.ui.viewmodel.HistoryViewModel
import java.time.format.DateTimeFormatter

/**
 * F25: History Screen
 * View archived financial data and trigger month-end reset
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    // Collect state from ViewModel
    val archives by viewModel.archives.collectAsState()
    val stats by viewModel.archiveStats.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val showResetDialog = remember { mutableStateOf(false) }

    // Show error/success messages
    LaunchedEffect(uiState.error, uiState.successMessage) {
        // TODO: Show SnackBar for errors/success
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog.value = true }) {
                        Icon(Icons.Default.Refresh, "Month-End Reset")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading && archives.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { /* TODO: Export all history */ },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Download, "Export History")
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (archives.isEmpty()) {
            EmptyHistoryState(modifier = Modifier.padding(padding))
        } else {
            HistoryContent(
                archives = archives,
                stats = stats,
                onArchiveClick = { archive ->
                    viewModel.getArchiveByMonth(archive.yearMonth.year, archive.yearMonth.monthValue)
                },
                onDeleteArchive = { archive ->
                    viewModel.deleteArchive(archive.id)
                },
                onRestoreArchive = { archive ->
                    viewModel.restoreArchive(archive.id)
                },
                modifier = Modifier.padding(padding)
            )
        }
    }

    // Month-End Reset Dialog
    if (showResetDialog.value) {
        MonthEndResetDialog(
            onConfirm = {
                showResetDialog.value = false
                viewModel.archiveCurrentMonth()
            },
            onDismiss = { showResetDialog.value = false }
        )
    }
}

@Composable
fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "No History Yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Archived months will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the refresh icon to archive current month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun HistoryContent(
    archives: List<ArchivedMonth>,
    stats: ArchiveStats?,
    onArchiveClick: (ArchivedMonth) -> Unit,
    onDeleteArchive: (ArchivedMonth) -> Unit,
    onRestoreArchive: (ArchivedMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics Summary
        item {
            Text(
                text = "Historical Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (stats != null) {
            item {
                StatisticsCard(stats)
            }
        }

        // Archived Months Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Archived Months (${archives.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Archive List
        items(archives) { archive ->
            ArchiveCard(
                archive = archive,
                onClick = { onArchiveClick(archive) },
                onDelete = { onDeleteArchive(archive) },
                onRestore = { onRestoreArchive(archive) }
            )
        }

        // Bottom Spacer
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatisticsCard(stats: ArchiveStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "All-Time Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total Months",
                    value = stats.totalMonths.toString(),
                    icon = Icons.Default.CalendarMonth
                )
                StatItem(
                    label = "Avg Savings",
                    value = "%.1f%%".format(stats.averageSavingsRate),
                    icon = Icons.Default.Savings
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total Income",
                    value = "₹%.0f".format(stats.totalIncome),
                    icon = Icons.Default.TrendingUp
                )
                StatItem(
                    label = "Total Expense",
                    value = "₹%.0f".format(stats.totalExpense),
                    icon = Icons.Default.TrendingDown
                )
            }

            if (stats.bestMonth != null) {
                Divider(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Best Month",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stats.bestMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (stats.worstMonth != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Worst Month",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = stats.worstMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveCard(
    archive: ArchivedMonth,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    val balanceColor = if (archive.balance >= 0)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.error

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = archive.yearMonth.format(formatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (archive.isRestored) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "Restored",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Divider()

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricColumn(
                    label = "Income",
                    value = "₹%.2f".format(archive.totalIncome),
                    color = MaterialTheme.colorScheme.primary
                )
                MetricColumn(
                    label = "Expense",
                    value = "₹%.2f".format(archive.totalExpense),
                    color = MaterialTheme.colorScheme.error
                )
                MetricColumn(
                    label = "Balance",
                    value = "₹%.2f".format(archive.balance),
                    color = balanceColor
                )
            }

            // Savings Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Savings Rate",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "%.1f%%".format(archive.savingsRate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        archive.savingsRate >= 20 -> MaterialTheme.colorScheme.primary
                        archive.savingsRate >= 10 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRestore,
                    modifier = Modifier.weight(1f),
                    enabled = !archive.isRestored
                ) {
                    Icon(Icons.Default.Restore, null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restore")
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun MetricColumn(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun MonthEndResetDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Month-End Reset",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("This will:")
                Text("• Archive current month's financial data")
                Text("• Clear expenses for new month")
                Text("• Carry forward income balance")
                Text("• Mark borrow/lend transactions")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This action cannot be undone!",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Confirm Reset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
