package com.budakattu.sante.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.runtime.collectAsState
import com.budakattu.sante.feature.auth.AuthViewModel
import com.budakattu.sante.feature.auth.LoginScreen
import com.budakattu.sante.feature.auth.OnboardingScreen
import com.budakattu.sante.feature.auth.SignupScreen
import com.budakattu.sante.feature.auth.SplashRoute
import com.budakattu.sante.feature.catalog.ui.CatalogRoute
import com.budakattu.sante.feature.leader.LeaderDashboardRoute
import com.budakattu.sante.feature.leader.LeaderInsightsRoute
import com.budakattu.sante.feature.leader.LeaderInventoryRoute
import com.budakattu.sante.feature.leader.LeaderProductEntryRoute
import com.budakattu.sante.feature.orders.BuyerOrdersRoute
import com.budakattu.sante.feature.orders.BuyerProfileRoute
import com.budakattu.sante.feature.orders.CartRoute
import com.budakattu.sante.feature.orders.LeaderOrdersRoute
import com.budakattu.sante.feature.orders.OrderConfirmationRoute
import com.budakattu.sante.feature.orders.OrderDetailRoute
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
                onOpenRoute = { route ->
                    navController.navigate(route)
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(NavRoutes.HERITAGE) {
            HeritageRouteScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(NavRoutes.ORDERS) {
            BuyerOrdersRoute(
                activeRoute = NavRoutes.ORDERS,
                marketRoute = NavRoutes.CATALOG,
                cartRoute = NavRoutes.CART,
                profileRoute = NavRoutes.PROFILE,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onOpenCart = { navController.navigate(NavRoutes.CART) },
                onOpenOrder = { orderId ->
                    navController.navigate("${NavRoutes.ORDER_DETAIL_PREFIX}/$orderId")
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(NavRoutes.CART) {
            CartRoute(
                activeRoute = NavRoutes.CART,
                marketRoute = NavRoutes.CATALOG,
                cartRoute = NavRoutes.CART,
                profileRoute = NavRoutes.PROFILE,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() },
                onOpenConfirmation = { orderId ->
                    navController.navigate("${NavRoutes.ORDER_CONFIRMATION_PREFIX}/$orderId") {
                        popUpTo(NavRoutes.CART) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = NavRoutes.ORDER_CONFIRMATION,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { backStackEntry ->
            OrderConfirmationRoute(
                orderId = backStackEntry.arguments?.getString("orderId").orEmpty(),
                onViewOrders = {
                    navController.navigate(NavRoutes.ORDERS) {
                        popUpTo(NavRoutes.CATALOG)
                    }
                },
                onBackToMarket = {
                    navController.navigate(NavRoutes.CATALOG) {
                        popUpTo(NavRoutes.CATALOG)
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = NavRoutes.ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) {
            OrderDetailRoute(
                onBack = { navController.popBackStack() },
            )
        }
        composable(NavRoutes.PROFILE) {
            val authViewModel: AuthViewModel = hiltViewModel()
            BuyerProfileRoute(
                activeRoute = NavRoutes.PROFILE,
                marketRoute = NavRoutes.CATALOG,
                cartRoute = NavRoutes.CART,
                profileRoute = NavRoutes.PROFILE,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(NavRoutes.AUTH_GRAPH) {
                        popUpTo(NavRoutes.BUYER_GRAPH) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = NavRoutes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "budakattu://product/{productId}" }),
        ) {
            ProductDetailRoute(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.leaderGraph(navController: NavHostController) {
    navigation(startDestination = NavRoutes.LEADER_HOME, route = NavRoutes.LEADER_GRAPH) {
        composable(NavRoutes.LEADER_HOME) {
            val authViewModel: AuthViewModel = hiltViewModel()
            LeaderDashboardRoute(
                onAddProduct = {
                    navController.navigate(NavRoutes.LEADER_PRODUCT_FORM_PREFIX)
                },
                onOpenOrders = {
                    navController.navigate(NavRoutes.LEADER_ORDERS)
                },
                onOpenInventory = {
                    navController.navigate(NavRoutes.LEADER_INVENTORY)
                },
                onOpenInsights = {
                    navController.navigate(NavRoutes.LEADER_INSIGHTS)
                },
                onEditDraft = { productId ->
                    navController.navigate("${NavRoutes.LEADER_PRODUCT_FORM_PREFIX}?productId=$productId")
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(NavRoutes.AUTH_GRAPH) {
                        popUpTo(NavRoutes.LEADER_GRAPH) { inclusive = true } }
                },
            )
        }
        composable(NavRoutes.LEADER_ORDERS) {
            LeaderOrdersRoute(
                onBack = { navController.popBackStack() },
            )
        }
        composable(NavRoutes.LEADER_INVENTORY) {
            LeaderInventoryRoute(
                onBack = { navController.popBackStack() },
                onAddProduct = { navController.navigate(NavRoutes.LEADER_PRODUCT_FORM_PREFIX) }
            )
        }
        composable(NavRoutes.LEADER_INSIGHTS) {
            LeaderInsightsRoute(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = NavRoutes.LEADER_PRODUCT_FORM,
            arguments = listOf(navArgument("productId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }),
        ) {
            LeaderProductEntryRoute(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(NavRoutes.BUYER_GRAPH) {
                        popUpTo(NavRoutes.LEADER_HOME)
                    }
                }
            )
        }
    }
}
