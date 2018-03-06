package com.mnassa.data.extensions

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * Created by Peter on 2/26/2018.
 */
const val DEFAULT_LIMIT = 100

internal suspend inline fun <reified R> Task<R>.await(): R {
    return suspendCancellableCoroutine { continuation ->
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