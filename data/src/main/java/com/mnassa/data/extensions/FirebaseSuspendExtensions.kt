package com.mnassa.data.extensions

import com.google.firebase.database.*
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.domain.model.HasId
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

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
    return suspendCancellableCoroutine { continuation ->
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) = continuation.resumeWithException(exceptionHandler.handle(error.toException()))
            override fun onDataChange(snapshot: DataSnapshot?) = continuation.resume(snapshot.mapList())
        })
    }
}

internal suspend inline fun <reified T : Any> Query.await(exceptionHandler: ExceptionHandler): T? {
    return suspendCancellableCoroutine { continuation ->
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) = continuation.resumeWithException(exceptionHandler.handle(error.toException()))
            override fun onDataChange(snapshot: DataSnapshot?) = continuation.resume(snapshot.mapSingle())
        })
    }
}

internal suspend inline fun <reified T : HasId> loadPortion(
        databaseReference: DatabaseReference,
        path: String,
        offset: String? = null,
        limit: Int = DEFAULT_LIMIT,
        exceptionHandler: ExceptionHandler): List<T> {

    val ref = databaseReference.child(path)
    val query = if (offset == null) {
        ref.orderByKey().limitToFirst(limit)
    } else {
        //ignore first element (to avoid duplicates)
        ref.orderByKey().startAt(offset).limitToFirst(limit + 1)
    }

    return suspendCancellableCoroutine { continuation ->
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) = continuation.resumeWithException(exceptionHandler.handle(error.toException()))

            override fun onDataChange(snapshot: DataSnapshot?) {
                val result = snapshot.mapList<T>()
                        .filterIndexed { index, _ -> !(index == 0 && offset != null) }
                continuation.resume(result)
            }
        })
    }
}