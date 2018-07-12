package com.mnassa.data.extensions

import com.google.android.gms.tasks.Task
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.exception.FirebaseMappingException
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * Created by Peter on 2/26/2018.
 */
const val DEFAULT_LIMIT = 20

suspend inline fun <reified R> Task<R>.await(exceptionHandler: ExceptionHandler): R {
    val result = suspendCancellableCoroutine<R> { continuation ->
        addOnCompleteListener {
            if (it.isSuccessful) {
                continuation.resume(it.result)
            } else {
                continuation.resumeWithException(exceptionHandler.handle(requireNotNull(it.exception)))
            }
        }
    }
    return result
}

//ignore all mapping exceptions
internal val Exception.isSuppressed: Boolean
    get() =
        this is IllegalArgumentException ||
                this is IllegalStateException ||
                this is NullPointerException ||
                this is FirebaseMappingException ||
                this is ClassCastException
