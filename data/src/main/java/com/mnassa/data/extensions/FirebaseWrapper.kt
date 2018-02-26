package com.mnassa.data.extensions

import com.google.android.gms.tasks.Task
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created by Peter on 2/26/2018.
 */
const val DEFAULT_LIMIT = 10 //just for testing. TODO: replace to 100

internal suspend inline fun <reified R> Task<R>.await(): R {
    return suspendCoroutine { continuation ->
        addOnCompleteListener {
            if (it.isSuccessful) {
                continuation.resume(it.result)
            } else {
                continuation.resumeWithException(it.exception
                        ?: IllegalStateException("Task not completed and Exception is null"))
            }
        }
    }
}