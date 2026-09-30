package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Transactions : Screen("transactions")
    data object Reports : Screen("reports")
    data object Budget : Screen("budget")
    data object Profile : Screen("profile")
    data object AddIncome : Screen("add_income")
    data object AddExpense : Screen("add_expense")
    data object EditIncome : Screen("edit_income/{id}") {
        fun createRoute(id: Long) = "edit_income/$id"
    }
    data object EditExpense : Screen("edit_expense/{id}") {
        fun createRoute(id: Long) = "edit_expense/$id"
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Dashboard.route,
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    ),
    BottomNavItem(
        route = Screen.Transactions.route,
        title = "Transactions",
        selectedIcon = Icons.Filled.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong,
        testTag = "nav_transactions"
    ),
    BottomNavItem(
        route = Screen.Reports.route,
        title = "Reports",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        testTag = "nav_reports"
    ),
    BottomNavItem(
        route = Screen.Budget.route,
        title = "Budget",
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet,
        testTag = "nav_budget"
    ),
    BottomNavItem(
        route = Screen.Profile.route,
        title = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_profile"
    )
)
