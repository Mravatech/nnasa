package com.mnassa.data.extensions

import com.google.firebase.database.*
import com.mnassa.domain.model.HasId
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created by Peter on 2/26/2018.
 */
//////////////////////////////////// LOAD DATA WITHOUT CHANGES HANDLING /////////////////////////////
// Loading list of values
internal suspend inline fun <reified T : HasId> get(databaseReference: DatabaseReference, path: String): List<T> {
    return databaseReference.child(path).orderByKey().awaitList()
}

// Loading single value
internal suspend inline fun <reified T : Any> get(databaseReference: DatabaseReference, path: String, id: String): T? {
    return databaseReference.child(path).child(id).await()
}

internal suspend inline fun <reified T : Any> Query.awaitList(): List<T> {
    return suspendCoroutine { continuation ->
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) = continuation.resumeWithException(error.toException())
            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot == null) {
                    continuation.resume(emptyList())
                    return
                }
                continuation.resume(snapshot.mapList())
            }
        })
    }
}

internal suspend inline fun <reified T : Any> Query.await(): T? {
    return suspendCoroutine { continuation ->
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }
            override fun onDataChange(snapshot: DataSnapshot?) = continuation.resume(snapshot.mapSingle())
        })
    }
}

internal suspend inline fun <reified T : HasId> loadPortion(databaseReference: DatabaseReference, path: String, offset: String? = null, limit: Int = DEFAULT_LIMIT): List<T> {
    val ref = databaseReference.child(path)
    val query = if (offset == null) {
        ref.orderByKey().limitToFirst(limit)
    } else {
        //ignore first element (to avoid duplicates)
        ref.orderByKey().startAt(offset).limitToFirst(limit + 1)
    }

    return suspendCoroutine { continuation ->
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot == null) {
                    continuation.resume(emptyList())
                    return
                }

                val result = mapListOfValues<T>(snapshot)
                        .filterIndexed { index, _ -> !(index == 0 && offset != null) }

                continuation.resume(result)
            }
        })
    }
}