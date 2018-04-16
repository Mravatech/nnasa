package com.mnassa.data.extensions

import com.google.firebase.database.*
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.model.HasId
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import timber.log.Timber

/**
 * Created by Peter on 2/26/2018.
 */
//////////////////////////////////// LOAD DATA WITHOUT CHANGES HANDLING /////////////////////////////
// Loading list of values
internal suspend inline fun <reified T : HasId> get(databaseReference: DatabaseReference, path: String, exceptionHandler: ExceptionHandler): List<T> {
    return databaseReference.child(path).orderByKey().awaitList(exceptionHandler)
}

// Loading single value
internal suspend inline fun <reified T : Any> get(databaseReference: DatabaseReference, path: String, id: String, exceptionHandler: ExceptionHandler): T? {
    return databaseReference.child(path).child(id).await(exceptionHandler)
}

internal suspend inline fun <reified T : Any> Query.awaitList(exceptionHandler: ExceptionHandler): List<T> {
    val result = suspendCancellableCoroutine<List<T>> { continuation ->
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) =
                    continuation.resumeWithException(exceptionHandler.handle(error.toException()))
            override fun onDataChange(snapshot: DataSnapshot?) {
                try {
                    continuation.resume(snapshot.mapList())
                } catch (e: Exception) {
                    Timber.e(e)
                    continuation.resumeWithException(exceptionHandler.handle(e))
                }
            }
        })
    }
    return result
}

var start = System.currentTimeMillis()

internal suspend inline fun <reified T : Any> Query.await(exceptionHandler: ExceptionHandler): T? {
    val result = suspendCancellableCoroutine<T?> { continuation ->
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) =
                    continuation.resumeWithException(exceptionHandler.handle(error.toException()))
            override fun onDataChange(snapshot: DataSnapshot?) {
                try {
                    continuation.resume(snapshot.mapSingle())
                } catch (e: Exception) {
                    Timber.e(e)
                    continuation.resumeWithException(exceptionHandler.handle(e))
                }
            }
        })
    }
    return result
}

internal suspend inline fun <reified T : HasId> loadPortion(
        databaseReference: DatabaseReference,
        offset: String? = null,
        limit: Int = DEFAULT_LIMIT,
        exceptionHandler: ExceptionHandler): List<T> {

    val query = if (offset == null) {
        databaseReference.orderByKey().limitToFirst(limit)
    } else {
        //ignore first element (to avoid duplicates)
        databaseReference.orderByKey().startAt(offset).limitToFirst(limit + 1)
    }

    val result = suspendCancellableCoroutine<List<T>> { continuation ->
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) =
                    continuation.resumeWithException(exceptionHandler.handle(error.toException()))

            override fun onDataChange(snapshot: DataSnapshot?) {
                try {
                    val result = snapshot.mapList<T>()
                            .filterIndexed { index, _ -> !(index == 0 && offset != null) }
                    continuation.resume(result)
                } catch (e: Exception) {
                    Timber.e(e)
                    continuation.resumeWithException(exceptionHandler.handle(e))
                }
            }
        })
    }
    return result
}