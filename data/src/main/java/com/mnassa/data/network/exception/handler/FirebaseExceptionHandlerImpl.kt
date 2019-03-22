package com.mnassa.data.network.exception.handler

import com.google.firebase.FirebaseException
import com.google.firebase.database.DatabaseException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.mnassa.domain.exception.FirebaseMappingException
import com.mnassa.domain.exception.NotAuthorizedException
import java.lang.Exception

/**
 * Created by Peter on 3/2/2018.
 */
class FirebaseExceptionHandlerImpl : FirebaseExceptionHandler {
    override fun handle(firebaseException: FirebaseException, tag: String): Throwable {
        return when (firebaseException) {
            is FirebaseFirestoreException -> {
                when (firebaseException.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        // Normal flow of the application should never
                        // cause this exception.
                        createNotAuthorizedException(firebaseException, tag)
                    }
                    else -> firebaseException
                }
            }
            else -> firebaseException
        }
    }

    override fun handle(databaseeException: DatabaseException, tag: String): Throwable {
        return createNotAuthorizedException(databaseeException, tag)
    }

    override fun handle(mappingException: FirebaseMappingException, tag: String): Throwable {
        return mappingException
    }

    private fun createNotAuthorizedException(e: Exception, tag: String): NotAuthorizedException {
        val message = "${e.message ?: "not authorized (database exception)"}; path: [$tag]"
        return NotAuthorizedException(message, e)
    }
}