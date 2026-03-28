package com.djihad.shopnite.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

object FirebaseMessagingInitializer {
    private const val Tag = "FirebaseMessagingInit"

    fun initialize() {
        runCatching {
            val messaging = FirebaseMessaging.getInstance()
            messaging.isAutoInitEnabled = true

            messaging.token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(Tag, "Failed to fetch the FCM registration token.", task.exception)
                    return@addOnCompleteListener
                }

                Log.d(Tag, "FCM registration token is ready.")
            }

            messaging.subscribeToTopic(PushTopics.ShopReset).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(Tag, "Failed to subscribe to the shop reset topic.", task.exception)
                    return@addOnCompleteListener
                }

                Log.d(Tag, "Subscribed to the shop reset topic.")
            }
        }.onFailure { throwable ->
            Log.w(Tag, "Firebase Messaging initialization failed.", throwable)
        }
    }
}
