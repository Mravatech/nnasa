package com.mnassa.data.extensions

import com.google.firebase.firestore.*
import com.mnassa.core.addons.launchWorker
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.model.HasId
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.pagination.PaginationController
import com.mnassa.domain.pagination.PaginationObserver
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.yield
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
    pagination: PaginationController? = null,
    limit: Int = DEFAULT_LIMIT
): Channel<ListItemEvent<OutType>> {
    forDebug { Timber.i("#LISTEN# toValueChannelWithChangesHandling ${this.path}") }
    val mutex = Mutex()
    val channel = ArrayChannel<ListItemEvent<OutType>>(limit)

    var listenerFirestore: ListenerRegistration? = null
    var listenerPagination: PaginationObserver? = null

    var querySizeCurrent = 0L
    var isBusy = false

    // Removes the FireStore
    // listener.
    val doDispose = {
        listenerFirestore?.remove()
        listenerFirestore = null

        if (pagination != null) {
            listenerPagination?.let(pagination::removeObserver)
        }
    }

    val doProcess = processor@{ changes: MutableList<DocumentChange> ->
        if (channel.isClosedForSend) {
            doDispose()
            return@processor
        }

        launchWorker {
            // Synchronize the processing, so there's almost no chance that the change events
            // will be sent in a wrong order.
            mutex.lock()

            val querySizeSaved = querySizeCurrent

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
                    doDispose()
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
                                val msg =
                                    "Mapping exception: class: ${DbType::class.java.name} id: $path"
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
                        doDispose()
                    } else {
                        val event: ListItemEvent<OutType>?
                        try {
                            event = change.second.await()
                        } catch (e: Exception) {
                            val path = change.first.document.reference.path
                            val msg =
                                "Mapping exception: class: ${OutType::class.java.name} id: $path"
                            Timber.e(e, msg)
                            continue
                        }

                        if (event != null) try {
                            channel.send(event)
                        } catch (e: ClosedSendChannelException) {
                            // Continue to cancel all next running
                            // jobs.
                            doDispose()
                        }
                    }
                }

                yield()
            }

            if (querySizeSaved == querySizeCurrent) {
                isBusy = false
            }

            mutex.unlock()
        }
    }

    val doSubscribe: suspend (Long?) -> Unit = { querySizeLimit ->
        // Create new listener on a query with
        // set limit size.
        firestoreLockSuspend {
            listenerFirestore?.remove()
            listenerFirestore = queryBuilder(this@toValueChannelWithChangesHandling)
                .run {
                    // Apply limit restriction only if it
                    // is set.
                    querySizeLimit?.let(::limit) ?: this
                }
                .addSnapshotListener { dataSnapshot, e ->
                    if (e != null) {
                        channel.close(exceptionHandler.handle(e, path))
                        doDispose.invoke()
                        return@addSnapshotListener
                    }

                    dataSnapshot?.documentChanges?.let(doProcess)
                }
        }
    }

    pagination
        ?.apply {
            // Subscribe via the pagination
            // controller.

            // FIXME: With current implementation there's a small chance that
            // remove event will be lost between re-subscriptions.
            observe(
                object : PaginationObserver {
                    override val isBusy: Boolean
                        get() = isBusy

                    override fun onSizeChanged(size: Long) {
                        querySizeCurrent = size
                        isBusy = true

                        launch(DefaultDispatcher) {
                            doSubscribe(size)
                        }
                    }
                }.also {
                    listenerPagination = it
                }
            )
        }
        ?: run {
            // Subscribe without pagination
            // handling.
            doSubscribe(null)
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

