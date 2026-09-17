package com.duduapps.mybanks.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duduapps.mybanks.features.account.detail.AccountDetailScreen
import com.duduapps.mybanks.features.account.form.AccountFormScreen
import com.duduapps.mybanks.features.auth.LoginScreen
import com.duduapps.mybanks.features.feedback.FeedbackScreen
import com.duduapps.mybanks.features.main.MainScreen
import com.duduapps.mybanks.features.removeads.RemoveAdsScreen
import com.duduapps.mybanks.features.splash.SplashScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToAddAccount = {
                    navController.navigate(Routes.accountForm())
                },
                onNavigateToAccountDetail = { accountId ->
                    navController.navigate(Routes.accountDetail(accountId))
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN)
                },
                onNavigateToRemoveAds = {
                    navController.navigate(Routes.REMOVE_ADS)
                },
                onNavigateToFeedback = {
                    navController.navigate(Routes.FEEDBACK)
                },
            )
        }

        composable(
            route = Routes.ACCOUNT_DETAIL,
            arguments = listOf(
                navArgument("accountId") { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getLong("accountId") ?: 0L
            AccountDetailScreen(
                accountId = accountId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Routes.accountForm(id))
                },
            )
        }

        composable(
            route = Routes.ACCOUNT_FORM,
            arguments = listOf(
                navArgument("accountId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
            ),
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getLong("accountId")
            AccountFormScreen(
                accountId = accountId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = { navController.popBackStack() },
            )
        }

        composable(Routes.REMOVE_ADS) {
            RemoveAdsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.FEEDBACK) {
            FeedbackScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
