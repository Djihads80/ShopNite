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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.djihad.shopnite.R
import com.djihad.shopnite.model.AccountType
import com.djihad.shopnite.model.LanguageOption
import com.djihad.shopnite.model.SupportedLanguages
import com.djihad.shopnite.ui.components.SectionHeading
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSaveApiKey: (String) -> Unit,
    onValidateAndSaveProfile: (String, String, AccountType) -> Unit,
    onSaveApiLanguage: (String) -> Unit,
    onSaveAppLanguage: (String) -> Unit,
    onUpdateNotifications: (Boolean?, Boolean?) -> Unit,
    onOpenCredits: () -> Unit,
) {
    val context = LocalContext.current
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }

    var apiKey by rememberSaveable(uiState.settings.customApiKey) { mutableStateOf(uiState.settings.customApiKey) }
    var playerName by rememberSaveable(uiState.settings.playerName) { mutableStateOf(uiState.settings.playerName) }
    var accountTypeValue by rememberSaveable(uiState.settings.accountType.apiValue) {
        mutableStateOf(uiState.settings.accountType.apiValue)
    }
    val accountType = AccountType.fromApiValue(accountTypeValue)
    val draftDiffersFromSaved = playerName.trim() != uiState.settings.playerName ||
        accountType != uiState.settings.accountType
    val showProfileError = draftDiffersFromSaved && uiState.profileValidationMessage != null

    LaunchedEffect(playerName, accountTypeValue, apiKey, uiState.settings.playerName, uiState.settings.accountType) {
        if (!draftDiffersFromSaved) {
            return@LaunchedEffect
        }
        delay(700)
        onValidateAndSaveProfile(apiKey, playerName, accountType)
    }

    LaunchedEffect(apiKey, uiState.settings.customApiKey) {
        if (apiKey.trim() == uiState.settings.customApiKey.trim()) {
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
                supporting = stringResource(R.string.settings_supporting),
            )
        }

        item {
            SettingsCard(title = stringResource(R.string.settings_profile_title)) {
                Text(
                    text = stringResource(R.string.settings_profile_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.settings_profile_label)) },
                    isError = showProfileError,
                    supportingText = {
                        when {
                            uiState.isValidatingProfile && draftDiffersFromSaved -> {
                                Text(stringResource(R.string.settings_profile_checking))
                            }
                            showProfileError -> {
                                Text(uiState.profileValidationMessage.orEmpty())
                            }
                            playerName.trim().isBlank() -> {
                                Text(stringResource(R.string.settings_profile_blank_support))
                            }
                            else -> {
                                Text(stringResource(R.string.settings_profile_saved_support))
                            }
                        }
                    },
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
                if (uiState.isValidatingProfile && draftDiffersFromSaved) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(18.dp),
                            strokeWidth = 2.5.dp,
                        )
                        Text(
                            text = stringResource(R.string.settings_profile_validating),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item {
            LanguageCard(
                title = stringResource(R.string.settings_api_language_title),
                supporting = stringResource(R.string.settings_api_language_support),
                current = SupportedLanguages.api.firstOrNull { it.tag == uiState.settings.apiLanguageTag }
                    ?: SupportedLanguages.api.first(),
                options = SupportedLanguages.api,
                onSelect = { onSaveApiLanguage(it.tag) },
            )
        }

        item {
            LanguageCard(
                title = stringResource(R.string.settings_app_language_title),
                supporting = stringResource(R.string.settings_app_language_support),
                current = SupportedLanguages.app.firstOrNull { it.tag == uiState.settings.appLanguageTag }
                    ?: SupportedLanguages.app.first(),
                options = SupportedLanguages.app,
                onSelect = { onSaveAppLanguage(it.tag) },
            )
        }

        item {
            SettingsCard(title = stringResource(R.string.settings_notifications_title)) {
                NotificationToggleRow(
                    title = stringResource(R.string.settings_notify_returns_title),
                    subtitle = stringResource(R.string.settings_notify_returns_subtitle),
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
                    title = stringResource(R.string.settings_notify_leaving_title),
                    subtitle = stringResource(R.string.settings_notify_leaving_subtitle),
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
            SettingsCard(title = stringResource(R.string.settings_api_key_title)) {
                Text(
                    text = stringResource(R.string.settings_api_key_support),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.settings_api_key_label)) },
                    supportingText = {
                        Text(
                            if (apiKey.trim().isNotBlank()) {
                                stringResource(R.string.settings_api_key_custom)
                            } else if (uiState.settings.apiKey.trim().isNotBlank()) {
                                stringResource(R.string.settings_api_key_default)
                            } else {
                                stringResource(R.string.settings_api_key_missing)
                            },
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
            }
        }

        item {
            SettingsCard(title = stringResource(R.string.settings_credits_title)) {
                Text(
                    text = stringResource(R.string.settings_credits_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    onClick = onOpenCredits,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.common_open_credits))
                }
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
        modifier = Modifier.fillMaxWidth(),
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
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
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
