package com.mnassa.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mnassa.core.addons.launchWorker
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.UserProfileInteractor
import timber.log.Timber


class MnassaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.i("onMessageReceived $remoteMessage")

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
    }

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