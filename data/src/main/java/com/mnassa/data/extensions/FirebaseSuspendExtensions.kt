package com.mnassa.data.extensions

import com.google.firebase.database.*
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.model.HasId
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Created by Peter on 2/26/2018.
 */
//////////////////////////////////// LOAD DATA WITHOUT CHANGES HANDLING /////////////////////////////
// Loading list of values
internal suspend inline fun <reified T : Any> get(databaseReference: DatabaseReference, path: String, exceptionHandler: ExceptionHandler): List<T> {
    return databaseReference.child(path).orderByKey().awaitList(exceptionHandler)
}

// Loading single value
internal suspend inline fun <reified T : Any> get(databaseReference: DatabaseReference, path: String, id: String, exceptionHandler: ExceptionHandler): T? {
    return databaseReference.child(path).child(id).await(exceptionHandler)
}

internal suspend inline fun <reified T : Any> Query.awaitList(exceptionHandler: ExceptionHandler): List<T> {
    forDebug { Timber.i("#LISTEN# awaitList ${this.ref}") }
    val result = suspendCancellableCoroutine<List<T>> { continuation ->
        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) =
                    continuation.resumeWithException(exceptionHandler.handle(error.toException(), ref.path.toString()))

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    continuation.resume(snapshot.mapList())
                } catch (e: Exception) {
                    Timber.e(e)
                    continuation.resumeWithException(exceptionHandler.handle(e, ref.path.toString()))
                }
            }
        }
        addListenerForSingleValueEvent(listener)

        continuation.invokeOnCancellation { removeEventListener(listener) }
    }
    return result
}

internal suspend inline fun <reified T : Any> Query.await(exceptionHandler: ExceptionHandler): T? {
    forDebug { Timber.i("#LISTEN# await ${this.ref}") }
    val result = suspendCancellableCoroutine<T?> { continuation ->
        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) =
                    continuation.resumeWithException(exceptionHandler.handle(error.toException(), ref.path.toString()))

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    continuation.resume(snapshot.mapSingle())
                } catch (e: Exception) {
                    Timber.e(e)
                    continuation.resumeWithException(exceptionHandler.handle(e, ref.path.toString()))
                }
            }
        }
        addListenerForSingleValueEvent(listener)
        continuation.invokeOnCancellation { removeEventListener(listener) }
    }
    return result
}

/**
 * Gets the list of values at database reference bypassing the cache.
 *
 * Note that this method will result in an exception if the device is
 * offline.
 */
internal suspend inline fun <reified T : Any> DatabaseReference.awaitListBypassCache(exceptionHandler: ExceptionHandler): List<T> {
    forDebug { Timber.i("#LISTEN# awaitListBypassCache ${this.ref}") }
    return suspendCancellableCoroutine { continuation ->
        runTransaction(object : Transaction.Handler {
            override fun onComplete(error: DatabaseError?, isSuccess: Boolean, snapshot: DataSnapshot?) {
                // This is OK for `isSuccess` to be false or `error` be not null,
                // as long as we get valid snapshot. This will happen if we don't have
                // write access to a reference.
                if (snapshot != null) {
                    try {
                        continuation.resume(snapshot.mapList())
                    } catch (e: Exception) {
                        Timber.e(e)
                        continuation.resumeWithException(exceptionHandler.handle(e, ref.path.toString()))
                    }
                } else {
                    val e = error?.toException() ?: IllegalStateException()
                    continuation.resumeWithException(exceptionHandler.handle(e, ref.path.toString()))
                }
            }

            override fun doTransaction(data: MutableData): Transaction.Result {
                return Transaction.abort() // we don't want to write anything
            }
        })
    }
}

internal suspend inline fun <reified T : HasId> loadPortion(
        databaseReference: DatabaseReference,
        offset: String? = null,
        limit: Int = DEFAULT_LIMIT,
        exceptionHandler: ExceptionHandler): List<T> {

    val query = if (offset == null) {
        databaseReference.orderByKey().limitToLast(limit)
    } else {
        //ignore first element (to avoid duplicates)
        databaseReference.orderByKey().endAt(offset).limitToLast(limit + 1)
    }

    return query.awaitList<T>(exceptionHandler)
            .asReversed()
            .takeLast(limit)
}