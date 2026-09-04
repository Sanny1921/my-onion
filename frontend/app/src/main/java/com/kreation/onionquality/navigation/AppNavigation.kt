package com.kreation.onionquality.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kreation.onionquality.ui.components.AnimatedBottomNavigation
import com.kreation.onionquality.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreens = listOf("dashboard", "history", "reports", "profile")
    val showBottomNav = currentRoute in mainScreens

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                AnimatedBottomNavigation(
                    currentRoute = currentRoute ?: "dashboard",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
        ) {
            composable("splash",
                enterTransition = { androidx.compose.animation.fadeIn(tween(300)) },
                exitTransition = { androidx.compose.animation.fadeOut(tween(300)) }
            ) { SplashScreen(onNavigateToLogin = { navController.navigate("login") { popUpTo("splash") { inclusive = true } } }) }
            
            composable("login") { LoginScreen(onLoginSuccess = { navController.navigate("dashboard") { popUpTo("login") { inclusive = true } } }) }
            
            composable("dashboard") { DashboardScreen(onNewInspection = { navController.navigate("new_inspection") }) }
            composable("history") { InspectionHistoryScreen() }
            composable("reports") { ReportsScreen() }
            composable("profile") { ProfileScreen(onLogout = { navController.navigate("login") { popUpTo(0) } }) }
            
            composable("new_inspection") { NewInspectionScreen(
                onBack = { navController.popBackStack() },
                onCaptureImage = { navController.navigate("camera") },
                onStartAnalysis = { navController.navigate("ai_analysis") }
            )}
            
            composable("camera") { CameraScreen(
                onBack = { navController.popBackStack() },
                onPhotoCaptured = { navController.popBackStack() }
            )}
            
            composable("ai_analysis") { AIAnalysisScreen(
                onAnalysisComplete = { navController.navigate("quality_results") { popUpTo("new_inspection") { inclusive = true } } }
            )}
            
            composable("quality_results") { QualityResultsScreen(
                onViewDetailed = { navController.navigate("detailed_analysis") },
                onGenerateReport = { navController.navigate("quality_report") },
                onBackToDashboard = { navController.navigate("dashboard") { popUpTo(0) } }
            )}
            
            composable("detailed_analysis") { DetailedAnalysisScreen(onBack = { navController.popBackStack() }) }
            
            composable("quality_report") { QualityReportScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
