package com.mnassa.data.network.exception

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

/**
 * Created by Peter on 3/2/2018.
 */
class FirebaseExceptionHandlerImpl : FirebaseExceptionHandler {
    override fun handle(firebaseException: FirebaseException): Throwable {
        //TODO: handle FirebaseExceptions here

        return when (firebaseException) {
            is FirebaseAuthInvalidCredentialsException -> firebaseException
            else -> firebaseException
        }
    }
}