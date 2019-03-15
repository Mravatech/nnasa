package com.mnassa.data.network.exception.handler

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.DatabaseException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.mnassa.domain.exception.FirebaseMappingException
import com.mnassa.domain.exception.NotAuthorizedException

/**
 * Created by Peter on 3/2/2018.
 */
class FirebaseExceptionHandlerImpl : FirebaseExceptionHandler {
    override fun handle(firebaseException: FirebaseException, tag: String): Throwable {
        return when (firebaseException) {
            is FirebaseAuthInvalidCredentialsException -> firebaseException //invalid phone number or invalid code
            is FirebaseTooManyRequestsException -> firebaseException //too many requests
            is FirebaseFirestoreException -> {
                when (firebaseException.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        // Normal flow of the application should never
                        // cause this exception.
                        val message = "${firebaseException.message
                            ?: "not authorized (database exception)"}; path: [$tag]"
                        NotAuthorizedException(message, firebaseException)
                    }
                    else -> firebaseException
                }
            }
            else -> firebaseException
        }
    }

    override fun handle(databaseeException: DatabaseException, tag: String): Throwable {
        return NotAuthorizedException(databaseeException.message
                ?: "not authorized (database exception)", databaseeException)
    }

    override fun handle(mappingException: FirebaseMappingException, tag: String): Throwable {
        return mappingException
    }
}