package com.fairsplit.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fairsplit.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

/**
 * Dashboard Screen
 * 
 * Main home screen showing:
 * - Top app bar with avatar + notifications
 * - Greeting "Hello, [username]"
 * - Balance overview card with percentage change
 * - 2x2 summary grid (Income/Expenses/You Owe/You are Owed)
 * - Spending by category chart (placeholder)
 * - Recent activity list
 * - Floating action button (Add expense)
 * - Bottom navigation bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController = rememberNavController(),
    viewModel: BalanceDashboardViewModel = hiltViewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "User"
    
    // Collect data from ViewModel
    val dashboardState by viewModel.dashboardState.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalBorrowed by viewModel.totalBorrowed.collectAsState()
    val totalLent by viewModel.totalLent.collectAsState()
    val currentBalance by viewModel.currentBalance.collectAsState()
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Note: Avatar click now navigates to Profile screen
    // Logout is available in Settings
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        showLogoutDialog = false
                        // Navigate back to splash/login
                        navController.navigate("splash") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            DashboardTopBar(
                userName = userName,
                onNotificationClick = { /* TODO: Navigate to notifications */ },
                onAvatarClick = { navController.navigate("profile") }
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
        bottomBar = {
            DashboardBottomBar(
                selectedTab = 0,
                onTabSelected = { index ->
                    when (index) {
                        0 -> { /* Already on dashboard */ }
                        1 -> { /* TODO: Groups */ }
                        2 -> { navController.navigate("expense_list") }
                        3 -> { navController.navigate("settings") }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item { Spacer(modifier = Modifier.height(Spacing.xs)) }
            
            // Balance Overview Card
            item {
                BalanceCard(
                    balance = currentBalance,
                    percentageChange = 0.0 // TODO: Calculate from previous month
                )
            }
            
            // Summary Cards Grid
            item {
                SummaryCardsGrid(
                    income = totalIncome,
                    expenses = totalExpense,
                    youOwe = totalBorrowed,
                    youAreOwed = totalLent
                )
            }
            
            // Spending Chart
            item {
                SpendingChartCard(
                    totalSpent = 2349.00
                )
            }
            
            // Recent Activity Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md, bottom = Spacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { navController.navigate("expense_list") }) {
                        Text(
                            text = "View All",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Transaction List
            items(sampleTransactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
            
            item { Spacer(modifier = Modifier.height(Spacing.xl)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    userName: String,
    onNotificationClick: () -> Unit,
    onAvatarClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.screenPadding)
        ) {
            // Top row: Avatar + Notification
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar (clickable for logout)
                IconButton(
                    onClick = onAvatarClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Notification Icon
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            
            // Greeting
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "Hello, $userName",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

@Composable
fun BalanceCard(
    balance: Double,
    percentageChange: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = CustomShapes.Card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardPadding)
        ) {
            Text(
                text = "Remaining Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    text = "$${"%.2f".format(balance)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "${if (percentageChange >= 0) "+" else ""}${"%.1f".format(percentageChange)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (percentageChange >= 0) Positive else Negative,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SummaryCardsGrid(
    income: Double,
    expenses: Double,
    youOwe: Double,
    youAreOwed: Double
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Income",
                amount = income,
                icon = Icons.Default.ArrowDownward,
                iconColor = Positive,
                iconBackgroundColor = Positive.copy(alpha = 0.2f)
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Expenses",
                amount = expenses,
                icon = Icons.Default.ArrowUpward,
                iconColor = Negative,
                iconBackgroundColor = Negative.copy(alpha = 0.2f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "You Owe",
                amount = youOwe,
                icon = Icons.Default.ArrowUpward,
                iconColor = Warning,
                iconBackgroundColor = Warning.copy(alpha = 0.2f)
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "You are Owed",
                amount = youAreOwed,
                icon = Icons.Default.ArrowDownward,
                iconColor = Info,
                iconBackgroundColor = Info.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    icon: ImageVector,
    iconColor: Color,
    iconBackgroundColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, BorderLight),
        shape = CustomShapes.Card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$${"%.2f".format(amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

@Composable
fun SpendingChartCard(
    totalSpent: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, BorderLight),
        shape = CustomShapes.Card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Chart placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        CustomShapes.Card
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Chart",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${"%.1f".format(totalSpent / 1000)}k",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Category legend
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    CategoryLegend(
                        modifier = Modifier.weight(1f),
                        name = "Food & Drinks",
                        color = MaterialTheme.colorScheme.primary
                    )
                    CategoryLegend(
                        modifier = Modifier.weight(1f),
                        name = "Shopping",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    CategoryLegend(
                        modifier = Modifier.weight(1f),
                        name = "Transport",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    CategoryLegend(
                        modifier = Modifier.weight(1f),
                        name = "Bills",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryLegend(
    modifier: Modifier = Modifier,
    name: String,
    color: Color
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = CustomShapes.CardSmall
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(transaction.iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.icon,
                    contentDescription = transaction.category,
                    tint = transaction.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "${if (transaction.amount >= 0) "+" else ""}$${"%.2f".format(transaction.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (transaction.amount >= 0) Positive else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DashboardBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
            label = { Text("Dashboard", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.Group, "Groups") },
            label = { Text("Groups", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Receipt, "Activity") },
            label = { Text("Activity", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.Person, "Profile") },
            label = { Text("Profile", fontSize = 11.sp) }
        )
    }
}

// Data models
data class Transaction(
    val title: String,
    val category: String,
    val amount: Double,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBackgroundColor: Color
)

// Sample data
val sampleTransactions = listOf(
    Transaction(
        title = "Zara",
        category = "Shopping",
        amount = -124.50,
        icon = Icons.Default.ShoppingBag,
        iconColor = Primary,
        iconBackgroundColor = Primary.copy(alpha = 0.1f)
    ),
    Transaction(
        title = "Salary",
        category = "Income",
        amount = 2500.00,
        icon = Icons.Default.AccountBalanceWallet,
        iconColor = Positive,
        iconBackgroundColor = Positive.copy(alpha = 0.1f)
    ),
    Transaction(
        title = "Starbucks",
        category = "Food & Drinks",
        amount = -5.50,
        icon = Icons.Default.Restaurant,
        iconColor = Primary,
        iconBackgroundColor = Primary.copy(alpha = 0.1f)
    ),
    Transaction(
        title = "Metro Pass",
        category = "Transport",
        amount = -25.00,
        icon = Icons.Default.Train,
        iconColor = Primary,
        iconBackgroundColor = Primary.copy(alpha = 0.1f)
    )
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    FairSplitTheme {
        DashboardScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenDarkPreview() {
    FairSplitTheme(darkTheme = true) {
        DashboardScreen()
    }
}
