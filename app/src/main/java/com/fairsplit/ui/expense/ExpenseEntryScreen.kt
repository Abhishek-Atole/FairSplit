package com.fairsplit.ui.expense

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Expense Entry Screen (Jetpack Compose)
 * Allows users to add/edit personal expenses
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") }
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
            Text(
                text = "Track your personal expenses",
                style = MaterialTheme.typography.bodyLarge
            )
            
            // TODO: Implement expense entry form
            // - Amount input
            // - Category selector
            // - Description field
            // - Date picker
            // - Receipt upload
            
            Text(
                text = "⚠️ Implementation pending",
                color = MaterialTheme.colorScheme.error
            )
            
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Dashboard")
            }
        }
    }
}

@Composable
fun ExpenseListScreen(
    navController: NavController
) {
    // TODO: Implement expense list with filters
    Text("Expense List Screen - Implementation Pending")
}
