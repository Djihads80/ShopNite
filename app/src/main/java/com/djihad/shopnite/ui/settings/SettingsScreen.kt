package com.djihad.shopnite.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.djihad.shopnite.R
import com.djihad.shopnite.data.local.DEFAULT_FORTNITE_API_KEY
import com.djihad.shopnite.model.AccountType
import com.djihad.shopnite.model.LanguageOption
import com.djihad.shopnite.model.SupportedLanguages
import com.djihad.shopnite.ui.components.SectionHeading
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSaveApiKey: (String) -> Unit,
    onSaveProfile: (String, AccountType) -> Unit,
    onSaveApiLanguage: (String) -> Unit,
    onSaveAppLanguage: (String) -> Unit,
    onUpdateNotifications: (Boolean?, Boolean?) -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }

    var apiKey by rememberSaveable(uiState.settings.apiKey) { mutableStateOf(uiState.settings.apiKey) }
    var playerName by rememberSaveable(uiState.settings.playerName) { mutableStateOf(uiState.settings.playerName) }
    var accountTypeValue by rememberSaveable(uiState.settings.accountType.apiValue) {
        mutableStateOf(uiState.settings.accountType.apiValue)
    }
    val accountType = AccountType.fromApiValue(accountTypeValue)

    LaunchedEffect(playerName, accountTypeValue, uiState.settings.playerName, uiState.settings.accountType) {
        if (playerName.trim() == uiState.settings.playerName &&
            accountType == uiState.settings.accountType
        ) {
            return@LaunchedEffect
        }
        delay(400)
        onSaveProfile(playerName, accountType)
    }

    LaunchedEffect(apiKey, uiState.settings.apiKey) {
        if (apiKey.trim() == uiState.settings.apiKey) {
            return@LaunchedEffect
        }
        delay(400)
        onSaveApiKey(apiKey)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            SectionHeading(
                title = stringResource(R.string.title_settings),
                supporting = "Player profile, languages, notifications, and app info.",
            )
        }

        item {
            SettingsCard(title = "Player profile") {
                Text(
                    text = "Your username and platform are saved automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Fortnite username") },
                    singleLine = true,
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AccountType.entries.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = accountType == type,
                            onClick = { accountTypeValue = type.apiValue },
                            shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = AccountType.entries.size,
                            ),
                            label = { Text(type.label) },
                        )
                    }
                }
            }
        }

        item {
            LanguageCard(
                title = "API language",
                supporting = "Controls the language used by shop, cosmetics, and news data from Fortnite-API.",
                current = SupportedLanguages.api.firstOrNull { it.tag == uiState.settings.apiLanguageTag }
                    ?: SupportedLanguages.api.first(),
                options = SupportedLanguages.api,
                onSelect = { onSaveApiLanguage(it.tag) },
            )
        }

        item {
            LanguageCard(
                title = "App language",
                supporting = "Controls app locale and localized labels currently included in the app.",
                current = SupportedLanguages.app.firstOrNull { it.tag == uiState.settings.appLanguageTag }
                    ?: SupportedLanguages.app.first(),
                options = SupportedLanguages.app,
                onSelect = { onSaveAppLanguage(it.tag) },
            )
        }

        item {
            SettingsCard(title = "Notifications") {
                NotificationToggleRow(
                    title = "Wishlist item returns",
                    subtitle = "Notify when a wishlisted cosmetic appears in the current item shop.",
                    checked = uiState.settings.notifyWishlistReturns,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            maybeRequestNotificationsPermission(
                                context = context,
                                onNeedRequest = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            )
                        }
                        onUpdateNotifications(enabled, null)
                    },
                )
                NotificationToggleRow(
                    title = "Leaving shop soon",
                    subtitle = "Notify when a wishlisted item is about to leave the live shop.",
                    checked = uiState.settings.notifyWishlistLeavingSoon,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            maybeRequestNotificationsPermission(
                                context = context,
                                onNeedRequest = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            )
                        }
                        onUpdateNotifications(null, enabled)
                    },
                )
            }
        }

        item {
            SettingsCard(title = "Advanced API key override") {
                Text(
                    text = "A default Fortnite-API key is already included. Only change this if you want to use your own key.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Fortnite API key") },
                    supportingText = {
                        Text(
                            if (apiKey.trim() == DEFAULT_FORTNITE_API_KEY) {
                                "Using the built-in default key."
                            } else {
                                "Custom key override enabled."
                            },
                        )
                    },
                    singleLine = true,
                )
            }
        }

        item {
            SettingsCard(title = "Credits") {
                Text(
                    text = "Data is powered by Fortnite-API.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "ShopNite is an unofficial Fortnite companion app built with Jetpack Compose and Material 3.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    onClick = { uriHandler.openUri("https://fortnite-api.com/") },
                ) {
                    Text("Open Fortnite-API")
                }
            }
        }

        item {
            SettingsCard(title = "Open source licenses") {
                LicenseLine("AndroidX / Jetpack Compose", "Apache License 2.0")
                LicenseLine("Kotlin and kotlinx libraries", "Apache License 2.0")
                LicenseLine("Retrofit", "Apache License 2.0")
                LicenseLine("OkHttp", "Apache License 2.0")
                LicenseLine("Coil", "Apache License 2.0")
                Text(
                    text = "See each project repository for the full license text and notices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun LanguageCard(
    title: String,
    supporting: String,
    current: LanguageOption,
    options: List<LanguageOption>,
    onSelect: (LanguageOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    SettingsCard(title = title) {
        Text(
            text = supporting,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(current.label)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            expanded = false
                            onSelect(option)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun LicenseLine(
    library: String,
    license: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = library,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = license,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun maybeRequestNotificationsPermission(
    context: android.content.Context,
    onNeedRequest: () -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val granted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
    if (!granted) onNeedRequest()
}
