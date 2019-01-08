package com.mnassa.data.extensions

import com.google.firebase.firestore.*
import com.mnassa.core.addons.launchWorker
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.model.HasId
import com.mnassa.domain.model.ListItemEvent
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.sync.Mutex
import timber.log.Timber

private const val BATCH_SIZE = 10

/**
 * Created by Peter on 4/13/2018.
 */
////////////////////////////////////// LOAD DATA WITH CHANGES HANDLING //////////////////////////////
//sealed class ListItemEvent<T: Any>(item: T) {
//    class Added<T: Any>(item: T, previousChildName: String?) : ListItemEvent<T>(item)
//    class Moved<T: Any>(item: T, previousChildName: String?) : ListItemEvent<T>(item)
//    class Changed<T: Any>(item: T, previousChildName: String?) : ListItemEvent<T>(item)
//    class Removed<T: Any>(item: T) : ListItemEvent<T>(item)
//}
internal suspend inline fun <reified DbType : HasId, reified OutType : Any> CollectionReference.toValueChannelWithChangesHandling(
    exceptionHandler: ExceptionHandler,
    noinline queryBuilder: (CollectionReference) -> Query = { it },
    noinline mapper: suspend (DbType) -> OutType? = { it as OutType },
    limit: Int = DEFAULT_LIMIT
): Channel<ListItemEvent<OutType>> {
    forDebug { Timber.i("#LISTEN# toValueChannelWithChangesHandling ${this.path}") }
    val mutex = Mutex()
    val channel = ArrayChannel<ListItemEvent<OutType>>(limit)

    lateinit var listener: ListenerRegistration

    // Removes the FireStore
    // listener.
    val dispose = {
        listener.remove()
    }

    val processor = processor@{ changes: MutableList<DocumentChange> ->
        if (channel.isClosedForSend) {
            dispose.invoke()
            return@processor
        }

        launchWorker {
            // Synchronize the processing, so there's almost no chance that the change events
            // will be sent in a wrong order.
            mutex.lock()

            // Groups the changes by BATCH_SIZE changes in each
            // batch.
            val changesBatches = ArrayList<List<DocumentChange>>()
            run {
                var changesBatch = ArrayList<DocumentChange>()
                changes.forEachIndexed { i, documentChange ->
                    if ((i + 1) % BATCH_SIZE == 0) {
                        // Flush current group
                        changesBatches += changesBatch
                        changesBatch = ArrayList()
                    }

                    changesBatch.add(documentChange)
                }

                // Flush the last group
                changesBatches += changesBatch
            }

            // Process the changes by batches
            for (changesBatch in changesBatches) {
                if (channel.isClosedForSend) {
                    dispose.invoke()
                    break
                }

                val changesDeferred = changesBatch
                    .map { documentChange ->
                        documentChange to async {
                            // Map to network database model
                            val dbEntity = try {
                                documentChange.document.mapSingle<DbType>()
                            } catch (e: Exception) {
                                val path = documentChange.document.reference.path
                                val msg = "Mapping exception: class: ${DbType::class.java.name} id: $path"
                                Timber.e(e, msg)
                                null
                            }
                                ?: return@async null // we can't do anything, we don't even know its id

                            if (documentChange.type == DocumentChange.Type.REMOVED) {
                                // Remove the model by it, instead of creating an
                                // entity and then passing it to remove.
                                return@async ListItemEvent.Removed<OutType>(dbEntity.id)
                            } else {
                                // Map to out model
                                val outEntity = try {
                                    mapper(dbEntity)
                                } catch (e: Exception) {
                                    null
                                }

                                return@async if (outEntity == null) {
                                    ListItemEvent.Removed(dbEntity.id)
                                } else when (documentChange.type) {
                                    DocumentChange.Type.ADDED -> ListItemEvent.Added(outEntity)
                                    DocumentChange.Type.MODIFIED -> ListItemEvent.Changed(outEntity)
                                    else -> error("Unknown FireStore change type!")
                                }
                            }
                        }
                    }

                for (change in changesDeferred) {
                    // Check if the channel is already closed to cancel
                    // all deferred jobs.
                    if (channel.isClosedForSend) {
                        change.second.cancel()
                        dispose.invoke()
                    } else {
                        val event: ListItemEvent<OutType>?
                        try {
                            event = change.second.await()
                        } catch (e: Exception) {
                            val path = change.first.document.reference.path
                            val msg = "Mapping exception: class: ${OutType::class.java.name} id: $path"
                            Timber.e(e, msg)
                            continue
                        }

                        if (event != null) try {
                            channel.send(event)
                        } catch (e: ClosedSendChannelException) {
                            // Continue to cancel all next running
                            // jobs.
                            dispose.invoke()
                        }
                    }
                }

                yield()
            }

            mutex.unlock()
        }
    }

    firestoreLockSuspend {
        listener = queryBuilder(this)
            .addSnapshotListener { dataSnapshot, e ->
                if (e != null) {
                    channel.close(exceptionHandler.handle(e, path))
                    dispose.invoke()
                    return@addSnapshotListener
                }

                dataSnapshot?.documentChanges?.let(processor)
            }
    }

    return channel
}

// Subscribe to single value changes

internal suspend inline fun <reified T : Any> DocumentReference.toValueChannel(exceptionHandler: ExceptionHandler): ReceiveChannel<T?> {
    forDebug { Timber.i("#LISTEN# toValueChannel ${this.path}") }
    val channel = RendezvousChannel<T?>()
    lateinit var listener: ListenerRegistration

    firestoreLockSuspend {
        listener = addSnapshotListener { dataSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                channel.close(exceptionHandler.handle(firebaseFirestoreException, path))
                listener.remove()
                return@addSnapshotListener
            }

            firestoreLock {
                try {
                    if (channel.isClosedForSend) {
                        listener.remove()
                        return@firestoreLock
                    }

                    channel.send(dataSnapshot.mapSingle())
                } catch (e: Exception) {
                    Timber.e(e)
                    listener.remove()
                    channel.close(exceptionHandler.handle(e, path))
                }
            }
        }
    }

    return channel
}

internal suspend inline fun <reified T : Any> DocumentReference.toListChannel(exceptionHandler: ExceptionHandler): ReceiveChannel<List<T>> {
    forDebug { Timber.i("#LISTEN# toListChannel ${this.path}") }
    val channel = RendezvousChannel<List<T>>()
    lateinit var listener: ListenerRegistration

    firestoreLockSuspend {
        listener = addSnapshotListener { dataSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                channel.close(exceptionHandler.handle(firebaseFirestoreException, path))
                listener.remove()
                return@addSnapshotListener
            }

            firestoreLock {
                try {
                    if (channel.isClosedForSend) {
                        listener.remove()
                        return@firestoreLock
                    }

                    channel.send(dataSnapshot.mapList())
                } catch (e: Exception) {
                    Timber.e(e)
                    listener.remove()
                    channel.close(exceptionHandler.handle(e, path))
                }
            }
        }
    }

    return channel
}

internal suspend inline fun <reified T : Any> CollectionReference.toListChannel(exceptionHandler: ExceptionHandler): ReceiveChannel<List<T>> {
    forDebug { Timber.i("#LISTEN# toListChannel ${this.path}") }
    val channel = RendezvousChannel<List<T>>()
    lateinit var listener: ListenerRegistration

    firestoreLockSuspend {
        listener = addSnapshotListener { dataSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                channel.close(exceptionHandler.handle(firebaseFirestoreException, path))
                listener.remove()
                return@addSnapshotListener
            }

            firestoreLock {
                try {
                    if (channel.isClosedForSend) {
                        listener.remove()
                        return@firestoreLock
                    }

                    channel.send(dataSnapshot.mapList())
                } catch (e: Exception) {
                    Timber.e(e)
                    listener.remove()
                    channel.close(exceptionHandler.handle(e, path))
                }
            }
        }
    }

    return channel
}

