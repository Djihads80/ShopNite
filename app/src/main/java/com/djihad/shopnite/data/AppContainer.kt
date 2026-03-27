package com.djihad.shopnite.data

import android.content.Context
import com.djihad.shopnite.data.local.UserSettingsRepository
import com.djihad.shopnite.data.remote.FortniteApiService
import com.djihad.shopnite.data.remote.createFortniteApiService
import com.djihad.shopnite.data.repository.FortniteRepository

interface AppContainer {
    val fortniteRepository: FortniteRepository
    val userSettingsRepository: UserSettingsRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val apiService: FortniteApiService = createFortniteApiService()

    override val userSettingsRepository: UserSettingsRepository =
        UserSettingsRepository(context.applicationContext)

    override val fortniteRepository: FortniteRepository =
        FortniteRepository(apiService)
}
