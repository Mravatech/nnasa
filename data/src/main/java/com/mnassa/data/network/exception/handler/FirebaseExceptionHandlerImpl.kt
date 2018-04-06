package com.mnassa.data.network.exception.handler

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.DatabaseException
import com.mnassa.domain.exception.NotAuthorizedException

/**
 * Created by Peter on 3/2/2018.
 */
class FirebaseExceptionHandlerImpl : FirebaseExceptionHandler {
    override fun handle(firebaseException: FirebaseException): Throwable {
        //TODO: handleException FirebaseExceptions here

        return when (firebaseException) {
            is FirebaseAuthInvalidCredentialsException -> firebaseException //invalid phone number or invalid code
            is FirebaseTooManyRequestsException -> firebaseException //too many requests
            else -> firebaseException
        }
    }

    override fun handle(databaseeException: DatabaseException): Throwable {
        return NotAuthorizedException(databaseeException.message ?: "not authorized", databaseeException)
    }
}