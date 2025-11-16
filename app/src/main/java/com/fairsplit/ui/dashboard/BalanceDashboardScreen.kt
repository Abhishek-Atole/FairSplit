package com.fairsplit.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/**
 * F23: Balance Dashboard Screen
 * 
 * This is a wrapper that delegates to the new DashboardScreen implementation.
 * Keeping this file for backward compatibility with NavGraph imports.
 */
@Composable
fun BalanceDashboardScreen(navController: NavController) {
    DashboardScreen(navController = navController)
}
