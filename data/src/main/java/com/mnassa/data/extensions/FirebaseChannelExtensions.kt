package com.mnassa.data.extensions

import com.google.firebase.database.*
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.model.HasId
import com.mnassa.domain.model.ListItemEvent
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber

/**
 * Created by Peter on 2/26/2018.
 */
////////////////////////////////// LOAD DATA WITH CHANGES HANDLING /////////////////////////////////
// Subscribe to list of values changes
internal inline fun <reified T : Any> getListChannel(
        databaseReference: DatabaseReference,
        path: String,
        exceptionHandler: ExceptionHandler
): ReceiveChannel<List<T>> = databaseReference.child(path).toListChannel(exceptionHandler)

internal inline fun <reified T : Any> Query.toListChannel(exceptionHandler: ExceptionHandler): ReceiveChannel<List<T>> {
    forDebug { Timber.i("#LISTEN# toListChannel ${this.ref}") }
    val query = this
    val channel = RendezvousChannel<List<T>>()

    addValueEventListener(object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            channel.close(exceptionHandler.handle(error.toException(), query.ref.path))
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val listener = this
            launch {
                try {
                    if (channel.isClosedForSend) {
                        query.removeEventListener(listener)
                        return@launch
                    }

                    channel.send(dataSnapshot.mapList())
                } catch (e: Exception) {
                    Timber.e(e)
                    query.removeEventListener(listener)
                    channel.close(exceptionHandler.handle(e, query.ref.path))
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
    forDebug { Timber.i("#LISTEN# toValueChannel ${this.ref}") }
    val query = this
    val channel = RendezvousChannel<T?>()

    addValueEventListener(object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            channel.close(exceptionHandler.handle(error.toException(), query.ref.path))
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val listener = this
            launch {
                try {
                    if (channel.isClosedForSend) {
                        query.removeEventListener(listener)
                        return@launch
                    }

                    channel.send(dataSnapshot.mapSingle())
                } catch (e: Exception) {
                    Timber.e(e)
                    query.removeEventListener(listener)
                    channel.close(exceptionHandler.handle(e, query.ref.path))
                }
            }
        }
    })

    return channel
}


//////////////////////////// LOAD DATA WITHOUT CHANGES HANDLING USING PAGINATION/////////////////////
// Simple pagination without handling content changes
internal inline fun <reified DbType : HasId, reified OutType : Any> getValueChannelWithPagination(
        databaseReference: DatabaseReference,
        exceptionHandler: ExceptionHandler,
        noinline mapper: suspend (input: DbType) -> OutType? = { it as OutType },
        limit: Int = DEFAULT_LIMIT
): Channel<OutType> {
    forDebug { Timber.i("#LISTEN# getValueChannelWithPagination ${databaseReference.ref}") }
    val channel = ArrayChannel<OutType>(limit)
    launch {
        try {
            if (channel.isClosedForSend) {
                return@launch
            }

            var latestId: String? = null
            while (true) {
                val portion = loadPortion<DbType>(databaseReference, latestId, limit, exceptionHandler)
                latestId = portion.lastOrNull()?.id
                portion.forEach { mapper(it)?.apply { channel.send(this) } }
                if (portion.size < limit) {
                    channel.close()
                    break
                }
            }
        } catch (e: ClosedSendChannelException) {
            //skip this exception
        } catch (e: Exception) {
            Timber.e(e)
            channel.close(exceptionHandler.handle(e, databaseReference.path))
        }
    }
    return channel
}

internal inline fun <reified DbType : HasId, reified OutType : Any> DatabaseReference.toValueChannelWithPagination(
        exceptionHandler: ExceptionHandler,
        noinline mapper: suspend (input: DbType) -> OutType? = { it as OutType },
        limit: Int = DEFAULT_LIMIT): Channel<OutType> {
    return getValueChannelWithPagination(this, exceptionHandler, mapper, limit)
}

////////////////////////////////////// LOAD DATA WITH CHANGES HANDLING //////////////////////////////
//sealed class ListItemEvent<T: Any>(item: T) {
//    class Added<T: Any>(item: T, previousChildName: String?) : ListItemEvent<T>(item)
//    class Moved<T: Any>(item: T, previousChildName: String?) : ListItemEvent<T>(item)
//    class Changed<T: Any>(item: T, previousChildName: String?) : ListItemEvent<T>(item)
//    class Removed<T: Any>(item: T) : ListItemEvent<T>(item)
//}
internal inline fun <reified DbType : HasId, reified OutType : Any> DatabaseReference.toValueChannelWithChangesHandling(
        exceptionHandler: ExceptionHandler,
        noinline mapper: suspend (DbType) -> OutType? = { it as OutType },
        limit: Int = DEFAULT_LIMIT): Channel<ListItemEvent<OutType>> {
    forDebug { Timber.i("#LISTEN# toValueChannelWithChangesHandling ${this.ref}") }
    val channel = ArrayChannel<ListItemEvent<OutType>>(limit)

    lateinit var listener: ChildEventListener

    val MOVED = 1
    val CHANGED = 2
    val ADDED = 3
    val REMOVED = 4


    val emitter = { input: DataSnapshot, previousChildName: String?, eventType: Int ->
        launch {
            try {

                if (channel.isClosedForSend) {
                    removeEventListener(listener)
                    return@launch
                }

                val dbEntity = input.mapSingle<DbType>() ?: return@launch
                val outModel = withContext(DefaultDispatcher) { mapper(dbEntity) } ?: return@launch
                val result: ListItemEvent<OutType> = when (eventType) {
                    MOVED -> ListItemEvent.Moved(outModel, previousChildName)
                    CHANGED -> ListItemEvent.Changed(outModel, previousChildName)
                    ADDED -> ListItemEvent.Added(outModel, previousChildName)
                    REMOVED -> ListItemEvent.Removed(outModel)
                    else -> throw IllegalArgumentException("Illegal event type $eventType")
                }

                channel.send(result)
            } catch (e: Exception) {
                when {
                    e.isSuppressed -> {
                        Timber.e(e, "Suppressed exception: class: ${DbType::class.java.name} path: ${input.path}")
//                        removeEventListener(listener)
//                        channel.close(exceptionHandler.handle(FirebaseMappingException(input.path, e)))
                    }
                    else -> {
                        Timber.e(e)
                        removeEventListener(listener)
                        channel.close(exceptionHandler.handle(e, path))
                    }
                }
            }
        }
    }

    listener = object : ChildEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
            channel.close(exceptionHandler.handle(databaseError.toException(), path))
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            emitter(dataSnapshot, previousChildName, MOVED)
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
            emitter(dataSnapshot, previousChildName, CHANGED)
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            emitter(dataSnapshot, previousChildName, ADDED)
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            emitter(dataSnapshot, null, REMOVED)
        }
    }

    addChildEventListener(listener)

    return channel
}