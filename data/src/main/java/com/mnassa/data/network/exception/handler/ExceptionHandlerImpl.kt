package com.mnassa.data.network.exception.handler

import com.google.firebase.FirebaseException

/**
 * Created by Peter on 3/5/2018.
 */
class ExceptionHandlerImpl(
        firebaseExceptionHandlerLazy: () -> FirebaseExceptionHandler,
        networkExceptionHandlerLazy: () -> NetworkExceptionHandler) : ExceptionHandler {

    private val firebaseExceptionHandler by lazy(firebaseExceptionHandlerLazy)
    private val networkExceptionHandler by lazy(networkExceptionHandlerLazy)

    override fun handle(throwable: Throwable): Throwable {
        return if (throwable is FirebaseException) firebaseExceptionHandler.handle(throwable) else networkExceptionHandler.handle(throwable)
    }
}