package com.djihad.shopnite.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.djihad.shopnite.R
import com.djihad.shopnite.ui.cosmetics.CosmeticsScreen
import com.djihad.shopnite.ui.cosmetics.CosmeticsViewModel
import com.djihad.shopnite.ui.detail.CosmeticDetailScreen
import com.djihad.shopnite.ui.detail.CosmeticDetailViewModel
import com.djihad.shopnite.ui.home.HomeScreen
import com.djihad.shopnite.ui.home.HomeViewModel
import com.djihad.shopnite.ui.settings.CreditsScreen
import com.djihad.shopnite.ui.settings.DebugOpsScreen
import com.djihad.shopnite.ui.settings.SettingsScreen
import com.djihad.shopnite.ui.settings.SettingsViewModel
import com.djihad.shopnite.ui.shop.ShopScreen
import com.djihad.shopnite.ui.shop.ShopViewModel
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private data class TopDestination(
    val route: String,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val topDestinations = listOf(
    TopDestination("home", R.string.nav_home, Icons.Default.Home),
    TopDestination("shop", R.string.nav_shop, Icons.Default.Storefront),
    TopDestination("cosmetics", R.string.nav_cosmetics, Icons.Default.Checkroom),
    TopDestination("settings", R.string.nav_settings, Icons.Default.Settings),
)

private const val cosmeticDetailPattern = "cosmetic/{cosmeticId}"
private const val creditsRoute = "settings/credits"
private const val debugOpsRoute = "settings/debug"

fun cosmeticDetailRoute(cosmeticId: String): String = "cosmetic/${Uri.encode(cosmeticId)}"

@Composable
fun ShopNiteApp() {
    val context = LocalContext.current
    val application = context.applicationContext as com.djihad.shopnite.ShopNiteApplication
    val settingsRepository = application.appContainer.userSettingsRepository
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = topDestinations.any { it.route == currentRoute }
    val scope = rememberCoroutineScope()
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }
    val hasRequestedNotificationPermission by settingsRepository.settings
        .map { it.hasRequestedNotificationPermission }
        .collectAsStateWithLifecycle(initialValue = false)

    fun requestNotificationsPermission(markPrompted: Boolean = true) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (markPrompted) {
            scope.launch { settingsRepository.setNotificationPermissionRequested() }
        }
        if (!granted) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(hasRequestedNotificationPermission) {
        if (!hasRequestedNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationsPermission()
        }
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp,
                ) {
                    topDestinations.forEach { destination ->
                        val label = stringResource(destination.labelRes)
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navigateTopLevel(destination.route)
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = label,
                                )
                            },
                            label = { Text(label) },
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
                    onRefresh = viewModel::refreshAll,
                    onOpenSettings = { navigateTopLevel("settings") },
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
                    onSelectCollection = viewModel::selectCollection,
                    onOpenCosmetic = { navController.navigate(cosmeticDetailRoute(it)) },
                )
            }
            composable("settings") {
                val viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                SettingsScreen(
                    uiState = state,
                    onSaveApiKey = viewModel::saveApiKey,
                    onValidateAndSaveProfile = viewModel::validateAndSaveProfile,
                    onSaveApiLanguage = viewModel::saveApiLanguage,
                    onUpdateNotifications = viewModel::updateNotificationPreferences,
                    onOpenDebugOps = { navController.navigate(debugOpsRoute) },
                    onOpenCredits = { navController.navigate(creditsRoute) },
                )
            }
            composable(creditsRoute) {
                val viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                CreditsScreen(
                    onBack = { navController.popBackStack() },
                    debugMenuUnlocked = state.settings.debugMenuUnlocked,
                    onUnlockDebugMenu = viewModel::unlockDebugMenu,
                )
            }
            composable(debugOpsRoute) {
                val viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                DebugOpsScreen(
                    forceCosmeticNotificationButtonEnabled = state.settings.debugForceCosmeticNotificationButtonEnabled,
                    onBack = { navController.popBackStack() },
                    onForceSendWishlistNotification = {
                        requestNotificationsPermission(markPrompted = false)
                        viewModel.forceSendWishlistNotification()
                    },
                    onForceSendWishlistLeavingNotification = {
                        requestNotificationsPermission(markPrompted = false)
                        viewModel.forceSendWishlistLeavingNotification()
                    },
                    onSetForceCosmeticNotificationButtonEnabled = viewModel::setForceCosmeticNotificationButtonEnabled,
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
                    onToggleWishlist = {
                        if (!state.isWishlisted) {
                            requestNotificationsPermission(markPrompted = false)
                        }
                        viewModel.toggleWishlist()
                    },
                    onForceDebugWishlistNotification = {
                        requestNotificationsPermission(markPrompted = false)
                        viewModel.forceSendWishlistNotification()
                    },
                )
            }
        }
    }
}
