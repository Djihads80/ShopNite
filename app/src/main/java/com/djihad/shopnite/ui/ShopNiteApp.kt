package com.djihad.shopnite.ui

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.djihad.shopnite.ui.cosmetics.CosmeticsScreen
import com.djihad.shopnite.ui.cosmetics.CosmeticsViewModel
import com.djihad.shopnite.ui.detail.CosmeticDetailScreen
import com.djihad.shopnite.ui.detail.CosmeticDetailViewModel
import com.djihad.shopnite.ui.home.HomeScreen
import com.djihad.shopnite.ui.home.HomeViewModel
import com.djihad.shopnite.ui.settings.SettingsScreen
import com.djihad.shopnite.ui.settings.SettingsViewModel
import com.djihad.shopnite.ui.shop.ShopScreen
import com.djihad.shopnite.ui.shop.ShopViewModel

private data class TopDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val topDestinations = listOf(
    TopDestination("home", "Home", Icons.Default.Home),
    TopDestination("shop", "Shop", Icons.Default.Storefront),
    TopDestination("cosmetics", "Cosmetics", Icons.Default.Inventory2),
    TopDestination("settings", "Settings", Icons.Default.Settings),
)

private const val cosmeticDetailPattern = "cosmetic/{cosmeticId}"

fun cosmeticDetailRoute(cosmeticId: String): String = "cosmetic/${Uri.encode(cosmeticId)}"

@Composable
fun ShopNiteApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = topDestinations.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") {
                val viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                HomeScreen(
                    uiState = state,
                    onSaveProfile = viewModel::saveProfile,
                    onRefresh = viewModel::refreshAll,
                )
            }
            composable("shop") {
                val viewModel: ShopViewModel = viewModel(factory = AppViewModelProvider.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                ShopScreen(
                    uiState = state,
                    filteredItems = viewModel.filteredItems(),
                    onSearchChange = viewModel::updateSearch,
                    onSelectType = viewModel::selectType,
                    onOpenCosmetic = { navController.navigate(cosmeticDetailRoute(it)) },
                )
            }
            composable("cosmetics") {
                val viewModel: CosmeticsViewModel = viewModel(factory = AppViewModelProvider.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                CosmeticsScreen(
                    uiState = state,
                    filteredItems = viewModel.filteredItems(),
                    onSearchChange = viewModel::updateSearch,
                    onSelectType = viewModel::selectType,
                    onSetShowNewOnly = viewModel::setShowNewOnly,
                    onOpenCosmetic = { navController.navigate(cosmeticDetailRoute(it)) },
                )
            }
            composable("settings") {
                val viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                SettingsScreen(
                    uiState = state,
                    onSaveApiKey = viewModel::saveApiKey,
                    onSaveProfile = viewModel::saveProfile,
                    onSaveApiLanguage = viewModel::saveApiLanguage,
                    onSaveAppLanguage = viewModel::saveAppLanguage,
                    onUpdateNotifications = viewModel::updateNotificationPreferences,
                )
            }
            composable(
                route = cosmeticDetailPattern,
                arguments = listOf(navArgument("cosmeticId") { type = NavType.StringType }),
            ) {
                val viewModel: CosmeticDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                CosmeticDetailScreen(
                    uiState = state,
                    onBack = { navController.popBackStack() },
                    onToggleWishlist = viewModel::toggleWishlist,
                )
            }
        }
    }
}
