package com.mnassa.data.extensions

import android.os.SystemClock
import com.google.android.gms.tasks.Tasks
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
import kotlinx.coroutines.experimental.sync.withLock
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
    val channel = ArrayChannel<ListItemEvent<OutType>>(limit)

    val registrations: MutableList<ListenerRegistration> = ArrayList()

    // Removes the FireStore
    // listener.
    val doDispose = {
        // Remove all of the existing
        // registrations.
        registrations.forEach(ListenerRegistration::remove)
    }

    val doProcessMutex = Mutex()
    val doProcess = processor@{ changes: MutableList<DocumentChange>, onComplete: () -> Unit ->
        if (channel.isClosedForSend) {
            doDispose()
            return@processor
        }

        launchWorker {
            // Synchronize the processing, so there's almost no chance that the change events
            // will be sent in a wrong order.
            doProcessMutex.lock()

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
                    firestoreLockSuspend {
                        doDispose()
                    }
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

                        firestoreLockSuspend {
                            doDispose()
                        }
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
                            firestoreLockSuspend {
                                doDispose()
                            }
                        }
                    }
                }

                yield()
            }

            // Mark that we have finished loading data
            // this time.
            onComplete()

            doProcessMutex.unlock()
        }
    }

    val doSubscribe: suspend (DocumentSnapshot?, DocumentSnapshot?, () -> Unit) -> Unit =
        { docStartAfter, docEndAt, onProcessComplete ->
            firestoreLockSuspend {
                val registration = queryBuilder(this@toValueChannelWithChangesHandling)
                    .run {
                        val query = docStartAfter?.let(::startAfter) ?: this
                        return@run docEndAt?.let(query::endAt) ?: query
                    }
                    .addSnapshotListener { dataSnapshot, e ->
                        if (e != null) {
                            channel.close(exceptionHandler.handle(e, path))
                            doDispose()
                            return@addSnapshotListener
                        }

                        dataSnapshot?.documentChanges?.let { changes ->
                            doProcess(changes, onProcessComplete)
                        }
                    }

                // Add registration to global
                // list.
                registrations.add(registration)
            }
        }

    pagination
        ?.apply {
            // Subscribe via the pagination
            // controller.
            observe(
                object : PaginationObserver {
                    private val mutex = Mutex()

                    @Volatile
                    private var prevDoc: DocumentSnapshot? = null

                    @Volatile
                    private var curToken: Long = 0L

                    @Volatile
                    override var isBusy: Boolean = false

                    override fun onNextPageRequested(limit: Long) {
                        if (channel.isClosedForSend) {
                            // User will have to start from
                            // the beginning.
                            return
                        }

                        val token = SystemClock.elapsedRealtimeNanos()

                        isBusy = true
                        curToken = token

                        launch(DefaultDispatcher) {
                            mutex.withLock {
                                val lastDoc = try {
                                    val task = queryBuilder(this@toValueChannelWithChangesHandling)
                                        .run {
                                            prevDoc?.let(::startAfter) ?: this
                                        }
                                        .limit(size)
                                        .get()

                                    Tasks.await(task).documents.lastOrNull()
                                } catch (e: FirebaseFirestoreException) {
                                    return@withLock
                                }

                                // Add the subscription
                                doSubscribe(prevDoc, lastDoc) {
                                    if (prevDoc == lastDoc && curToken == token) {
                                        isBusy = false
                                    }
                                }

                                prevDoc = lastDoc
                            }
                        }
                    }
                }
            )
        }
        ?: run {
            // Subscribe without pagination
            // handling.
            doSubscribe(null, null) {}
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

