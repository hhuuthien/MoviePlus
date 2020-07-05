package com.thien.movieplus

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("MyFCM", "From: " + remoteMessage.from)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("MyFCM", "Message data payload: " + remoteMessage.data)
        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d("MyFCM", "Message Notification Body: " + remoteMessage.notification!!.body)
        }
    }

    override fun onNewToken(token: String) {
        Log.d("MyFCM", "Refreshed token: $token")
        //sendRegistrationToServer(token)
    }
}