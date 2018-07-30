package com.mnassa.data.network.exception.handler

import com.google.firebase.FirebaseException
import com.google.firebase.database.DatabaseException
import com.mnassa.domain.exception.FirebaseMappingException

/**
 * Created by Peter on 3/5/2018.
 */
class ExceptionHandlerImpl(
        firebaseExceptionHandlerLazy: () -> FirebaseExceptionHandler,
        networkExceptionHandlerLazy: () -> NetworkExceptionHandler) : ExceptionHandler {

    private val firebaseExceptionHandler by lazy(firebaseExceptionHandlerLazy)
    private val networkExceptionHandler by lazy(networkExceptionHandlerLazy)

    override fun handle(throwable: Throwable, tag: String): Throwable {
        return when (throwable) {
            is FirebaseException -> firebaseExceptionHandler.handle(throwable, tag)
            is DatabaseException -> firebaseExceptionHandler.handle(throwable, tag)
            is FirebaseMappingException -> firebaseExceptionHandler.handle(throwable, tag)
            else -> networkExceptionHandler.handle(throwable)
        }
    }
}