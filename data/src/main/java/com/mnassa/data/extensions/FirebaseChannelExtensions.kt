package com.mnassa.data.extensions

import com.google.firebase.database.*
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.domain.model.HasId
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.*
import timber.log.Timber

/**
 * Created by Peter on 2/26/2018.
 */
////////////////////////////////// LOAD DATA WITH CHANGES HANDLING /////////////////////////////////
// Subscribe to list of values changes
internal inline fun <reified T : Any> getListChannel(databaseReference: DatabaseReference, path: String, exceptionHandler: ExceptionHandler): ReceiveChannel<List<T>> {
    return databaseReference.child(path).toListChannel(exceptionHandler)
}

internal inline fun <reified T : Any> Query.toListChannel(exceptionHandler: ExceptionHandler): ReceiveChannel<List<T>> {
    val query = this
    val channel = RendezvousChannel<List<T>>()

    addValueEventListener(object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            channel.close(exceptionHandler.handle(error.toException()))
        }

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val listener = this
            async {
                try {
                    channel.send(dataSnapshot.mapList())
                } catch (e: ClosedSendChannelException) {
                    query.removeEventListener(listener)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    })

    return channel
}


// Subscribe to single value changes
internal inline fun <reified T : Any> getValueChannel(databaseReference: DatabaseReference, path: String, id: String, exceptionHandler: ExceptionHandler): ReceiveChannel<T?> {
    return databaseReference.child(path).child(id).toValueChannel(exceptionHandler)
}

internal inline fun <reified T : Any> Query.toValueChannel(exceptionHandler: ExceptionHandler): ReceiveChannel<T?> {
    val query = this
    val channel = RendezvousChannel<T?>()

    addValueEventListener(object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            channel.close(exceptionHandler.handle(error.toException()))
        }

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val listener = this
            async {
                try {
                    channel.send(dataSnapshot.mapSingle())
                } catch (e: ClosedSendChannelException) {
                    query.removeEventListener(listener)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    })

    return channel
}


//////////////////////////// LOAD DATA WITHOUT CHANGES HANDLING USING PAGINATION/////////////////////
// Simple pagination without handling content changes
internal inline fun <reified T : HasId> load(databaseReference: DatabaseReference, path: String, limit: Int = DEFAULT_LIMIT, exceptionHandler: ExceptionHandler): Channel<T> {
    val channel = ArrayChannel<T>(limit)
    async {
        try {
            var latestId: String? = null
            while (true) {
                val portion = loadPortion<T>(databaseReference, path, latestId, limit, exceptionHandler)
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