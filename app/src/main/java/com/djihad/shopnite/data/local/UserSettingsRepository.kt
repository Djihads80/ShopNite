package com.djihad.shopnite.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.djihad.shopnite.model.AccountType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.userSettingsDataStore by preferencesDataStore(name = "shopnite_user_settings")

const val DEFAULT_FORTNITE_API_KEY = "df018965-d546-4984-92ed-7dd3171af366"

data class UserSettings(
    val apiKey: String = DEFAULT_FORTNITE_API_KEY,
    val customApiKey: String = "",
    val playerName: String = "",
    val accountType: AccountType = AccountType.Epic,
    val apiLanguageTag: String = "en",
    val appLanguageTag: String = "system",
    val notifyWishlistReturns: Boolean = true,
    val notifyWishlistLeavingSoon: Boolean = true,
    val hasRequestedNotificationPermission: Boolean = false,
    val wishlist: Set<String> = emptySet(),
    val notifiedReturnIds: Set<String> = emptySet(),
    val notifiedLeavingTokens: Set<String> = emptySet(),
)

class UserSettingsRepository(private val context: Context) {
    val settings: Flow<UserSettings> = context.userSettingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map(::toUserSettings)

    suspend fun saveProfile(name: String, accountType: AccountType) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[Keys.PlayerName] = name.trim()
            preferences[Keys.AccountType] = accountType.apiValue
        }
    }

    suspend fun saveApiKey(apiKey: String) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[Keys.ApiKey] = apiKey.trim()
        }
    }

    suspend fun saveApiLanguage(tag: String) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[Keys.ApiLanguage] = tag
        }
    }

    suspend fun saveAppLanguage(tag: String) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[Keys.AppLanguage] = tag
        }
    }

    suspend fun saveNotificationPreferences(
        notifyReturns: Boolean? = null,
        notifyLeavingSoon: Boolean? = null,
    ) {
        context.userSettingsDataStore.edit { preferences ->
            notifyReturns?.let { preferences[Keys.NotifyReturns] = it }
            notifyLeavingSoon?.let { preferences[Keys.NotifyLeavingSoon] = it }
        }
    }

    suspend fun setNotificationPermissionRequested() {
        context.userSettingsDataStore.edit { preferences ->
            preferences[Keys.NotificationPermissionRequested] = true
        }
    }

    suspend fun toggleWishlist(cosmeticId: String) {
        context.userSettingsDataStore.edit { preferences ->
            val current = preferences[Keys.Wishlist].orEmpty().toMutableSet()
            if (!current.add(cosmeticId)) {
                current.remove(cosmeticId)
            }
            preferences[Keys.Wishlist] = current
        }
    }

    suspend fun setNotifiedReturnIds(ids: Set<String>) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[Keys.NotifiedReturnIds] = ids
        }
    }

    suspend fun setNotifiedLeavingTokens(tokens: Set<String>) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[Keys.NotifiedLeavingTokens] = tokens
        }
    }

    private fun toUserSettings(preferences: Preferences): UserSettings = UserSettings(
        apiKey = preferences[Keys.ApiKey]?.takeIf { it.isNotBlank() } ?: DEFAULT_FORTNITE_API_KEY,
        customApiKey = preferences[Keys.ApiKey].orEmpty(),
        playerName = preferences[Keys.PlayerName].orEmpty(),
        accountType = AccountType.fromApiValue(preferences[Keys.AccountType]),
        apiLanguageTag = preferences[Keys.ApiLanguage] ?: "en",
        appLanguageTag = preferences[Keys.AppLanguage] ?: "system",
        notifyWishlistReturns = preferences[Keys.NotifyReturns] ?: true,
        notifyWishlistLeavingSoon = preferences[Keys.NotifyLeavingSoon] ?: true,
        hasRequestedNotificationPermission = preferences[Keys.NotificationPermissionRequested] ?: false,
        wishlist = preferences[Keys.Wishlist].orEmpty(),
        notifiedReturnIds = preferences[Keys.NotifiedReturnIds].orEmpty(),
        notifiedLeavingTokens = preferences[Keys.NotifiedLeavingTokens].orEmpty(),
    )

    private object Keys {
        val ApiKey = stringPreferencesKey("api_key")
        val PlayerName = stringPreferencesKey("player_name")
        val AccountType = stringPreferencesKey("account_type")
        val ApiLanguage = stringPreferencesKey("api_language")
        val AppLanguage = stringPreferencesKey("app_language")
        val NotifyReturns = booleanPreferencesKey("notify_returns")
        val NotifyLeavingSoon = booleanPreferencesKey("notify_leaving_soon")
        val NotificationPermissionRequested = booleanPreferencesKey("notification_permission_requested")
        val Wishlist = stringSetPreferencesKey("wishlist")
        val NotifiedReturnIds = stringSetPreferencesKey("notified_return_ids")
        val NotifiedLeavingTokens = stringSetPreferencesKey("notified_leaving_tokens")
    }
}
