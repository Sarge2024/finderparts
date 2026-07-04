package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.AuthViewModel
import com.example.auth.LoginScreen
import com.example.data.AppDatabase
import com.example.data.PartRepository
import com.example.ui.PartAssociationScreen
import com.example.ui.PartAssociationViewModel
import com.example.ui.PartAssociationViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room DB & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = PartRepository(database.partDao())

        // Initialize ViewModels
        val partViewModel: PartAssociationViewModel by viewModels {
            PartAssociationViewModelFactory(repository)
        }
        val authViewModel: AuthViewModel by viewModels()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val user by authViewModel.user.collectAsState()

                    if (user != null) {
                        PartAssociationScreen(
                            viewModel = partViewModel,
                            onLogout = { authViewModel.logout() }
                        )
                    } else {
                        LoginScreen(authViewModel = authViewModel)
                    }
                }
            }
        }
    }
}
