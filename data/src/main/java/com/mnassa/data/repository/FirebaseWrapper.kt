package com.mnassa.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.mnassa.domain.models.Model
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.*
import timber.log.Timber
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created by Peter on 2/23/2018.
 */
private const val DEFAULT_LIMIT = 10 //just for testing. TODO: replace to 100

//////////////////////////////////////////// MAPPING ///////////////////////////////////////////////

private inline fun <reified T : Model> mapSingleValue(dataSnapshot: DataSnapshot?): T? {
    return dataSnapshot?.run {
        val res = getValue(T::class.java)
        res?.id = key
        res
    }
}

private inline fun <reified T : Model> mapListOfValues(dataSnapshot: DataSnapshot?): List<T> {
    if (dataSnapshot == null) return emptyList()
    return dataSnapshot.children.map { requireNotNull(mapSingleValue<T>(it)) }
}

////////////////////////////////// LOAD DATA WITH CHANGES HANDLING /////////////////////////////////
// Subscribe to list of values changes
internal inline fun <reified T : Model> getObservable(databaseReference: DatabaseReference, path: String): ReceiveChannel<List<T>> {
    val channel = RendezvousChannel<List<T>>()

    databaseReference.child(path).addValueEventListener(object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            channel.close(error.toException())
        }

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val listener = this
            async {
                try {
                    channel.send(mapListOfValues(dataSnapshot))
                } catch (e: ClosedSendChannelException) {
                    databaseReference.child(path).removeEventListener(listener)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    })

    return channel
}

// Subscribe to single value changes
internal inline fun <reified T : Model> getObservable(databaseReference: DatabaseReference, path: String, id: String): ReceiveChannel<T?> {
    val channel = RendezvousChannel<T?>()

    databaseReference.child(path).child(id).addValueEventListener(object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            channel.close(error.toException())
        }

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val listener = this
            async {
                try {
                    channel.send(mapSingleValue(dataSnapshot))
                } catch (e: ClosedSendChannelException) {
                    databaseReference.child(path).removeEventListener(listener)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    })

    return channel
}

//////////////////////////////////// LOAD DATA WITHOUT CHANGES HANDLING /////////////////////////////
// Loading list of values
internal suspend inline fun <reified T : Model> get(databaseReference: DatabaseReference, path: String): List<T> {
    return loadPortion(databaseReference, path, null, Int.MAX_VALUE)
}

// Loading single value
internal suspend inline fun <reified T : Model> get(databaseReference: DatabaseReference, path: String, id: String): T? {
    return suspendCoroutine { continuation ->
        databaseReference.child(path).child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                continuation.resume(mapSingleValue(snapshot))
            }
        })
    }
}

//////////////////////////// LOAD DATA WITHOUT CHANGES HANDLING USING PAGINATION/////////////////////
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
        } catch (e: Exception) {
            Timber.e(e)
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

                val result = mapListOfValues<T>(snapshot)
                        .filterIndexed { index, _ -> !(index == 0 && offset != null) }

                continuation.resume(result)
            }
        })
    }
}
////////////////////////////////////////////////////////////////////////////////////////////////////