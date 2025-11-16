package com.fairsplit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fairsplit.ui.auth.SplashScreen
import com.fairsplit.ui.auth.LoginScreen
import com.fairsplit.ui.auth.CreateAccountScreen
import com.fairsplit.ui.dashboard.BalanceDashboardScreen
import com.fairsplit.ui.income.IncomeEntryScreen
import com.fairsplit.ui.income.IncomeHistoryScreen
import com.fairsplit.ui.expense.ExpenseEntryScreen
import com.fairsplit.ui.expense.ExpenseListScreen
import com.fairsplit.ui.borrowlend.BorrowLendEntryScreen
import com.fairsplit.ui.borrowlend.BorrowLendDashboardScreen
import com.fairsplit.ui.report.MonthlyReportScreen
import com.fairsplit.ui.history.HistoryScreen
import com.fairsplit.ui.screens.AddExpenseScreen
import com.fairsplit.ui.screens.ExpenseListScreen
import com.fairsplit.ui.settings.SettingsScreen
import com.fairsplit.ui.profile.ProfileScreen
import com.fairsplit.ui.profile.ChangePasswordScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    onLaunchGoogleSignIn: ((onSuccess: () -> Unit, onFailure: (Exception) -> Unit) -> Unit)? = null
) {
    // Check Firebase Auth state
    var isUserLoggedIn by remember { mutableStateOf<Boolean?>(null) }
    var showError by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        isUserLoggedIn = currentUser != null
    }
    
    // Determine start destination based on auth state
    val startDestination = when (isUserLoggedIn) {
        true -> Screen.Dashboard.route
        false -> Screen.Splash.route
        null -> Screen.Splash.route // Show splash while checking
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ===== AUTH FLOW =====
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    // TODO: Implement forgot password screen
                },
                onGoogleSignIn = {
                    onLaunchGoogleSignIn?.invoke(
                        {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        { exception ->
                            android.util.Log.e("NavGraph", "Google Sign-In failed", exception)
                            // TODO: Show error to user
                        }
                    )
                },
                onAppleSignIn = {
                    // TODO: Implement Apple Sign-In
                }
            )
        }
        
        composable(Screen.SignUp.route) {
            CreateAccountScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onGoogleSignUp = {
                    onLaunchGoogleSignIn?.invoke(
                        {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.SignUp.route) { inclusive = true }
                            }
                        },
                        { exception ->
                            android.util.Log.e("NavGraph", "Google Sign-Up failed", exception)
                            // TODO: Show error to user
                        }
                    )
                },
                onAppleSignUp = {
                    // TODO: Implement Apple Sign-Up
                }
            )
        }
        
        // ===== MAIN APP FLOW =====
        composable(Screen.Dashboard.route) {
            BalanceDashboardScreen(navController = navController)
        }
        
        composable(Screen.IncomeEntry.route) {
            IncomeEntryScreen(navController = navController)
        }
        
        composable(Screen.IncomeHistory.route) {
            IncomeHistoryScreen(navController = navController)
        }
        
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(navController = navController)
        }
        
        composable(Screen.ExpenseEntry.route) {
            ExpenseEntryScreen(navController = navController)
        }
        
        composable(Screen.ExpenseList.route) {
            com.fairsplit.ui.screens.ExpenseListScreen(navController = navController)
        }
        
        composable(Screen.BorrowLendEntry.route) {
            BorrowLendEntryScreen(navController = navController)
        }
        
        composable(Screen.BorrowLendList.route) {
            BorrowLendDashboardScreen(navController = navController)
        }
        
        composable(Screen.MonthlyReport.route) {
            MonthlyReportScreen(navController = navController)
        }
        
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(navController = navController)
        }
    }
}

sealed class Screen(val route: String) {
    // Auth screens
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    
    // Main app screens
    object Dashboard : Screen("dashboard")
    object IncomeEntry : Screen("income_entry")
    object IncomeHistory : Screen("income_history")
    object AddExpense : Screen("add_expense")
    object ExpenseEntry : Screen("expense_entry")
    object ExpenseList : Screen("expense_list")
    object BorrowLendEntry : Screen("borrowlend_entry")
    object BorrowLendList : Screen("borrowlend_list")
    object MonthlyReport : Screen("monthly_report")
    object History : Screen("history")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object ChangePassword : Screen("change_password")
}
