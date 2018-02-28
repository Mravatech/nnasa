package com.mnassa.data.network.exception

import kotlinx.coroutines.experimental.Deferred

/**
 * Created by Peter on 28.02.2018.
 */
internal suspend fun <T> Deferred<T>.handleNetworkException(handler: NetworkExceptionHandler): T {
    try {
        return await()
    } catch (e: Throwable) {
        throw handler.handle(e)
    }
}