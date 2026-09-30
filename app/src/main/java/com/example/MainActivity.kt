package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.data.database.AppDatabase
import com.example.data.repository.ExpenseTrackRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.ExpenseTrackTheme
import com.example.ui.viewmodel.ExpenseTrackViewModel
import com.example.ui.viewmodel.ExpenseTrackViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ExpenseTrackRepository(
            incomeDao = database.incomeDao(),
            expenseDao = database.expenseDao(),
            budgetDao = database.budgetDao()
        )
        val viewModelFactory = ExpenseTrackViewModelFactory(repository)

        setContent {
            ExpenseTrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val viewModel: ExpenseTrackViewModel = viewModel(factory = viewModelFactory)
                    AppNavigation(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
