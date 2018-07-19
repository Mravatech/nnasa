package com.mnassa.data.network.exception.handler

import com.google.firebase.FirebaseException
import com.google.firebase.database.DatabaseException
import com.mnassa.domain.exception.FirebaseMappingException

/**
 * Created by Peter on 3/2/2018.
 */
interface FirebaseExceptionHandler {
    fun handle(firebaseException: FirebaseException, tag: String): Throwable
    fun handle(databaseeException: DatabaseException, tag: String): Throwable
    fun handle(mappingException: FirebaseMappingException, tag: String): Throwable
}