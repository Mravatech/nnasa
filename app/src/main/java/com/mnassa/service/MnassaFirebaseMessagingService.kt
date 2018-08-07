package com.mnassa.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mnassa.core.addons.launchWorker
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.UserProfileInteractor
import timber.log.Timber


/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/12/2018
 */
class MnassaFirebaseMessagingService : FirebaseMessagingService() {


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            val data = remoteMessage.data
            if (data["amount"] != null) {
                val intent = Intent()
                intent.putExtra(AMOUNT, data["amount"])
                intent.putExtra(FROM_USER, data["fromUser"])
                intent.action = NOTIFICATION
                sendOrderedBroadcast(intent, null)
            }
        }

        // Check if message contains a notification payload.
        //if (remoteMessage.notification != null) {
        //}


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        Timber.i("onNewToken >>> addPushToken >>> $token")

        val userProfileInteractor: UserProfileInteractor = applicationContext.getInstance()

        launchWorker {
            userProfileInteractor.addPushToken(token)
        }
    }

    companion object {
        const val NOTIFICATION = "NOTIFICATION"
        const val AMOUNT = "AMOUNT"
        const val FROM_USER = "FROM_USER"
    }

}