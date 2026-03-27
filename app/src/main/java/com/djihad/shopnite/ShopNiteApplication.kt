package com.djihad.shopnite

import android.app.Application
import com.djihad.shopnite.data.AppContainer
import com.djihad.shopnite.data.DefaultAppContainer
import com.djihad.shopnite.notifications.NotificationChannels
import com.djihad.shopnite.notifications.NotificationScheduler

class ShopNiteApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)
        NotificationChannels.create(this)
        NotificationScheduler.scheduleShopMonitor(this)
    }
}
