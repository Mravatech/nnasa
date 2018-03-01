package com.mnassa.data.network.exception

import kotlinx.coroutines.experimental.Deferred

/**
 * Created by Peter on 28.02.2018.
 */
internal suspend fun <T> Deferred<T>.handleNetworkException(handler: NetworkExceptionHandler): T {
    try {
        val result = await()
        return result
    } catch (e: Throwable) {
        throw handler.handle(e)
    }
}