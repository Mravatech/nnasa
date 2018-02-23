package com.mnassa.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.mnassa.domain.models.Model
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ArrayChannel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ClosedSendChannelException
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created by Peter on 2/23/2018.
 */
private const val DEFAULT_LIMIT = 10 //just for testing. TODO: replace to 100

/////////////////////////////////////////////////////////////////////////////////////////////////////
//

/////////////////////////////////////////////////////////////////////////////////////////////////////
// Loading single value
internal suspend inline fun <reified T : Model> get(databaseReference: DatabaseReference, path: String, id: String): T? {
    return suspendCoroutine { continuation ->
        databaseReference.child(path).child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                continuation.resume(snapshot?.run {
                    val res = getValue(T::class.java)
                    res?.id = id
                    res
                })
            }
        })
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////
// Simple pagination without handling content changes
internal inline fun <reified T : Model> load(databaseReference: DatabaseReference, path: String, limit: Int = DEFAULT_LIMIT): Channel<T> {
    val channel = ArrayChannel<T>(limit)
    async {
        try {
            var latestId: String? = null
            while (true) {
                val portion = loadPortion<T>(databaseReference, path, latestId, limit)
                latestId = portion.lastOrNull()?.id
                portion.forEach { channel.send(it) }
                if (portion.size < limit) {
                    channel.close()
                    break
                }
            }
        } catch (e: ClosedSendChannelException) {
            //skip this exception
        }
    }
    return channel
}

private suspend inline fun <reified T : Model> loadPortion(databaseReference: DatabaseReference, path: String, offset: String? = null, limit: Int = DEFAULT_LIMIT): List<T> {
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

                val result = snapshot.children.map {
                    val mapped = requireNotNull(it.getValue(T::class.java))
                    mapped.id = it.key
                    mapped
                }.filterIndexed { index, _ -> !(index == 0 && offset != null) }

                continuation.resume(result)
            }
        })
    }
}
////////////////////////////////////////////////////////////////////////////////////////////////////