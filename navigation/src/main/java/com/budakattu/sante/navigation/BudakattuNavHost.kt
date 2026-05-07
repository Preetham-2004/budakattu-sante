package com.budakattu.sante.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.budakattu.sante.feature.auth.AuthViewModel
import com.budakattu.sante.feature.auth.LoginScreen
import com.budakattu.sante.feature.auth.OnboardingScreen
import com.budakattu.sante.feature.auth.SignupScreen
import com.budakattu.sante.feature.auth.SplashRoute
import com.budakattu.sante.feature.catalog.ui.CatalogRoute
import com.budakattu.sante.feature.leader.LeaderDashboardScreen
import com.budakattu.sante.feature.productdetail.ProductDetailRoute

@Composable
fun BudakattuNavHost(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.AUTH_GRAPH,
        modifier = modifier,
    ) {
        authGraph(navController)
        buyerGraph(navController)
        leaderGraph(navController)
    }
}

private fun androidx.navigation.NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(startDestination = NavRoutes.SPLASH, route = NavRoutes.AUTH_GRAPH) {
        composable(NavRoutes.SPLASH) {
            SplashRoute(
                onNavigateToOnboarding = {
                    navController.navigate(NavRoutes.ONBOARDING) { popUpTo(NavRoutes.SPLASH) { inclusive = true } }
                },
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) { popUpTo(NavRoutes.SPLASH) { inclusive = true } }
                },
                onNavigateToBuyerHome = {
                    navController.navigate(NavRoutes.BUYER_GRAPH) { popUpTo(NavRoutes.AUTH_GRAPH) { inclusive = true } }
                },
                onNavigateToLeaderHome = {
                    navController.navigate(NavRoutes.LEADER_GRAPH) { popUpTo(NavRoutes.AUTH_GRAPH) { inclusive = true } }
                },
            )
        }
        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(
                onContinue = { navController.navigate(NavRoutes.LOGIN) },
            )
        }
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onLogin = { navController.navigate(NavRoutes.SPLASH) { popUpTo(NavRoutes.LOGIN) { inclusive = true } } },
                onSignup = { navController.navigate(NavRoutes.SIGNUP) },
            )
        }
        composable(NavRoutes.SIGNUP) {
            SignupScreen(
                onSignupComplete = { navController.navigate(NavRoutes.SPLASH) { popUpTo(NavRoutes.SIGNUP) { inclusive = true } } },
                onLogin = { navController.navigate(NavRoutes.LOGIN) { popUpTo(NavRoutes.SIGNUP) { inclusive = true } } },
            )
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.buyerGraph(navController: NavHostController) {
    navigation(startDestination = NavRoutes.CATALOG, route = NavRoutes.BUYER_GRAPH) {
        composable(NavRoutes.CATALOG) {
            CatalogRoute(
                onOpenProduct = { productId ->
                    navController.navigate("${NavRoutes.PRODUCT_DETAIL_PREFIX}/$productId")
                },
            )
        }
        composable(
            route = NavRoutes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "budakattu://product/{productId}" }),
        ) {
            ProductDetailRoute()
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.leaderGraph(navController: NavHostController) {
    navigation(startDestination = NavRoutes.LEADER_HOME, route = NavRoutes.LEADER_GRAPH) {
        composable(NavRoutes.LEADER_HOME) {
            val authViewModel: AuthViewModel = hiltViewModel()
            LeaderDashboardScreen(
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(NavRoutes.AUTH_GRAPH) {
                        popUpTo(NavRoutes.LEADER_GRAPH) { inclusive = true }
                    }
                },
            )
        }
    }
}
