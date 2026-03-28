package com.djihad.shopnite.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ShopNiteFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d(Tag, "Received a new FCM registration token.")
        FirebaseMessaging.getInstance().subscribeToTopic(PushTopics.ShopReset).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(Tag, "Failed to resubscribe to the shop reset topic.", task.exception)
                return@addOnCompleteListener
            }

            Log.d(Tag, "Resubscribed to the shop reset topic.")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        NotificationChannels.create(applicationContext)

        val title = remoteMessage.data["title"]
            ?: remoteMessage.notification?.title
            ?: "Fortnite Item Shop Reset"
        val body = remoteMessage.data["body"]
            ?: remoteMessage.notification?.body
            ?: "The item shop has refreshed."
        val channelId = remoteMessage.data["channelId"]
            ?.takeIf { it.isNotBlank() }
            ?: NotificationChannels.ShopReset
        val notificationId = remoteMessage.data["notificationId"]?.toIntOrNull() ?: 1003

        NotificationSupport.showTextNotification(
            context = applicationContext,
            channelId = channelId,
            notificationId = notificationId,
            title = title,
            body = body,
        )
    }

    companion object {
        private const val Tag = "ShopNiteFCM"
    }
}
