package com.example.antiscam.screens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.antiscam.screens.auth.AuthViewModel
import com.example.antiscam.screens.auth.OtpScreen
import com.example.antiscam.screens.auth.PhoneInputScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn = remember {
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
    }

    val startDestination = if (isLoggedIn) "main" else "phone_input"
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("phone_input") {
            PhoneInputScreen(
                viewModel = authViewModel,
                onNavigateToOtp = {
                    navController.navigate("otp")
                }
            )
        }

        composable("otp") {
            OtpScreen(
                navController = navController,
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("phone_input") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen()
        }
    }
}

