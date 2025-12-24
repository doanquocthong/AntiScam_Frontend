package com.example.antiscam.screens.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.antiscam.data.repository.CallLogRepository
import com.example.antiscam.screens.auth.AuthViewModel
import com.example.antiscam.screens.auth.OtpScreen
import com.example.antiscam.screens.auth.PhoneInputScreen
import com.example.antiscam.screens.contact.CallLogDetailScreen
import com.example.antiscam.screens.contact.CallLogDetailViewModel
import com.example.antiscam.screens.contact.CallLogDetailViewModelFactory
import com.example.antiscam.screens.contact.ContactScreen
import com.example.antiscam.screens.message.MessageDetailScreen
import com.example.antiscam.screens.message.MessageScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity

    val authViewModel: AuthViewModel = viewModel(activity)
    val isLoggedIn = remember {
        FirebaseAuth.getInstance().currentUser != null
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
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("phone_input") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(navController)
        }

        composable("messages") {
            MessageScreen(
                onOpenMessageDetail = { address ->
                    navController.navigate("conversation/$address")
                }
            )
        }

        composable(
            "message/{address}",
            arguments = listOf(navArgument("address") { type = NavType.StringType })
        ) {
            val address = it.arguments!!.getString("address")!!
            MessageDetailScreen(
                address = address,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "call_log/{phoneNumber}",
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) {
            val phoneNumber = it.arguments!!.getString("phoneNumber")!!

            CallLogDetailScreen(
                phoneNumber = phoneNumber,
                onBack = { navController.popBackStack() }
            )
        }
        composable("contacts") {
            ContactScreen(
                openCallLogDetail = { phoneNumber ->
                    navController.navigate("call_log/$phoneNumber")
                }
            )
        }

    }
}

