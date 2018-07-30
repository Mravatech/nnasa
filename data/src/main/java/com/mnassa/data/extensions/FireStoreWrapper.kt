package com.mnassa.data.extensions

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext

/**
 * Created by Peter on 7/18/2018.
 */

suspend fun <R> firestoreLockSuspend(function: suspend () -> R): R {
    return withContext(UI) { function() }
}

fun <R> firestoreLock(function: suspend () -> R): Deferred<R> {
    return async(UI) { function() }
}