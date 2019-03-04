package com.mnassa.data.extensions

import kotlinx.coroutines.*

/**
 * Created by Peter on 7/18/2018.
 */

suspend fun <R> firestoreLockSuspend(function: suspend () -> R): R {
    return withContext(Dispatchers.Main) { function() }
}

suspend fun <R> firestoreLock(function: suspend () -> R): Deferred<R> {
    return GlobalScope.async(Dispatchers.Main) { function() }
}