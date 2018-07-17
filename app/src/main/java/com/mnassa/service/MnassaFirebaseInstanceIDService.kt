package com.mnassa.service

import com.google.firebase.iid.FirebaseInstanceIdService
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.UserProfileInteractor
import kotlinx.coroutines.experimental.launch

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/12/2018
 */

class MnassaFirebaseInstanceIDService : FirebaseInstanceIdService() {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    override fun onTokenRefresh() {
        // Get updated InstanceID token.
        val userProfileInteractor: UserProfileInteractor = applicationContext.getInstance()

        launch {
            userProfileInteractor.addPushToken()
        }
    }
    // [END refresh_token]

}