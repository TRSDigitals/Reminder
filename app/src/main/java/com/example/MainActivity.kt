package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.models.AppLanguage
import com.example.ui.screens.*
import com.example.ui.theme.DinaSiriTheme
import com.example.ui.viewmodels.AuthUiState
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DinaSiriTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DinaSiriRootNav(
                        authViewModel = authViewModel,
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun DinaSiriRootNav(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val language by mainViewModel.currentLanguage.collectAsState()
    val navController = rememberNavController()

    // Request Audio & Notification Permissions gracefully
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    when (val state = authState) {
        is AuthUiState.Welcome -> {
            WelcomeScreen(
                language = language,
                onContinue = { authViewModel.onContinueFromWelcome() }
            )
        }

        is AuthUiState.EnterPhone, is AuthUiState.RestoringAccount -> {
            PhoneAuthScreen(
                authViewModel = authViewModel,
                language = language,
                onBackToWelcome = { authViewModel.onContinueFromWelcome() }
            )
        }

        is AuthUiState.EnterProfile -> {
            ProfileSetupScreen(
                authViewModel = authViewModel,
                phoneNumber = state.phoneNumber,
                initialLanguage = language
            )
        }

        is AuthUiState.Authenticated -> {
            // Main Application Navigation
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(
                        viewModel = mainViewModel,
                        onNavigateToCalendar = { navController.navigate("calendar") },
                        onNavigateToFarm = { navController.navigate("farm") },
                        onNavigateToCattle = { navController.navigate("cattle") },
                        onNavigateToRecords = { navController.navigate("records") },
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }

                composable("calendar") {
                    CalendarScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("farm") {
                    FarmScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("cattle") {
                    CattleScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("records") {
                    RecordsScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("recap") {
                    RecapScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() },
                        onLoggedOut = {
                            authViewModel.onContinueFromWelcome()
                        }
                    )
                }
            }
        }
    }
}
