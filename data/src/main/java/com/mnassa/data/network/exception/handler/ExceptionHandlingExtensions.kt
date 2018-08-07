package com.mnassa.data.network.exception.handler

import com.google.firebase.FirebaseException
import kotlinx.coroutines.experimental.Deferred

/**
 * Created by Peter on 28.02.2018.
 */
internal suspend fun <T> Deferred<T>.handleException(handler: NetworkExceptionHandler): T {
    try {
        val result = await()
        return result
    } catch (e: Throwable) {
        throw handler.handle(e)
    }
}

internal suspend fun <T> Deferred<T>.handleException(handler: ExceptionHandler): T {
    try {
        val result = await()
        return result
    } catch (e: Throwable) {
        throw handler.handle(e, this.onJoin.toString())
    }
}

internal fun FirebaseException.handleException(handler: FirebaseExceptionHandler): Throwable {
    return handler.handle(this, this.toString())
}

internal fun FirebaseException.handleException(handler: ExceptionHandler): Throwable {
    return handler.handle(this, this.toString())
}