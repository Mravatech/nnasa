package com.mnassa.data.network.exception

import com.google.firebase.FirebaseException

/**
 * Created by Peter on 3/2/2018.
 */
interface FirebaseExceptionHandler {
    fun handle(firebaseException: FirebaseException): Throwable
}