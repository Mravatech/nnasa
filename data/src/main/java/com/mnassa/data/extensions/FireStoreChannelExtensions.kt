package com.mnassa.data.extensions

import android.os.SystemClock
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.mnassa.core.addons.asyncWorker
import com.mnassa.core.addons.launchUI
import com.mnassa.core.addons.launchWorker
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.extensions.toCoroutineScope
import com.mnassa.domain.model.HasId
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.pagination.PaginationController
import com.mnassa.domain.pagination.PaginationObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

// Beware, stranger, you're entering the zone
// of async stuff.

private const val BATCH_SIZE = 10

internal suspend inline fun <reified DbType : HasId, reified OutType : Any> CollectionReference.toValueChannelWithChangesHandling(
    exceptionHandler: ExceptionHandler,
    noinline queryBuilder: (CollectionReference) -> Query = { it },
    noinline mapper: suspend (DbType) -> OutType? = { it as OutType },
    pagination: PaginationController? = null,
    limit: Int = DEFAULT_LIMIT
): Channel<ListItemEvent<OutType>> {
    forDebug { Timber.i("#LISTEN# toValueChannelWithChangesHandling ${this.path}") }
    val channel = Channel<ListItemEvent<OutType>>(limit)

    val registrations: MutableList<ListenerRegistration> = ArrayList()

    val queue = ArrayList<List<DocumentChange>>()
    val queueMutex = Mutex()
    val workerMutex = Mutex()

    val rootCoroutineScope = coroutineContext.toCoroutineScope()

    // Removes the FireStore
    // listener.
    val doDispose = {
        // Remove all of the existing
        // registrations.
        registrations.forEach(ListenerRegistration::remove)
    }

    val doProcess: (List<DocumentChange>, () -> Unit) -> Unit =
        { newChanges, onComplete ->
            // Add a snapshot to process to a
            // queue.
            runBlocking {
                queueMutex.withLock {
                    queue += newChanges
                }
            }

            rootCoroutineScope.launchWorker {
                if (channel.isClosedForSend) {
                    doDispose()
                    return@launchWorker
                }

                workerMutex.withLock {
                    val changes = queueMutex.withLock {
                        queue.flatten()
                            .also {
                                queue.clear()
                            }
                    }

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
                            GlobalScope.launchUI {
                                doDispose()
                            }
                            break
                        }

                        val changesDeferred = changesBatch
                            .map { documentChange ->
                                documentChange to asyncWorker {
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
                                        ?: return@asyncWorker null // we can't do anything, we don't even know its id

                                    if (documentChange.type == DocumentChange.Type.REMOVED) {
                                        // Remove the model by it, instead of creating an
                                        // entity and then passing it to remove.
                                        return@asyncWorker ListItemEvent.Removed<OutType>(dbEntity.id)
                                    } else {
                                        // Map to out model
                                        val outEntity = try {
                                            mapper(dbEntity)
                                        } catch (e: Exception) {
                                            null
                                        }

                                        return@asyncWorker if (outEntity == null) {
                                            ListItemEvent.Removed(dbEntity.id)
                                        } else when (documentChange.type) {
                                            DocumentChange.Type.ADDED -> ListItemEvent.Added(
                                                outEntity
                                            )
                                            DocumentChange.Type.MODIFIED -> ListItemEvent.Changed(
                                                outEntity
                                            )
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

                                GlobalScope.launchUI {
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
                                    GlobalScope.launchUI {
                                        doDispose()
                                    }
                                }
                            }
                        }

                        yield()
                    }
                }

              //  delay(5000L)

                onComplete()
            }
        }

    val doSubscribe: suspend (DocumentSnapshot?, DocumentSnapshot?, () -> Unit) -> Unit =
        { docStartAfter, docEndAt, onComplete ->
            rootCoroutineScope.launchUI {
                val registration = queryBuilder(this@toValueChannelWithChangesHandling)
                    .run {
                        val query = docStartAfter?.let(::startAfter) ?: this
                        return@run docEndAt?.let(query::endAt) ?: query
                    }
                    .addSnapshotListener { dataSnapshot, e ->
                        if (e != null) {
                            channel.close(exceptionHandler.handle(e, path))
                            return@addSnapshotListener
                        }

                        val changes = dataSnapshot?.documentChanges
                        if (changes != null) {
                            doProcess(changes, onComplete)
                        }
                    }

                // Add registration to global
                // list.
                registrations.add(registration)
            }
        }

    rootCoroutineScope.launchUI {
        // Subscribe to a FireStore query with/without
        // handling the pagination.
        if (pagination != null) {
            val observer = object : PaginationObserver {
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

                    rootCoroutineScope.launchWorker {
                        mutex.withLock {
                            val lastDoc = try {
                                val task = queryBuilder(this@toValueChannelWithChangesHandling)
                                    .run {
                                        prevDoc?.let(::startAfter) ?: this
                                    }
                                    .limit(limit)
                                    .get()

                                Tasks.await(task)
                                    .documents
                                    .lastOrNull()
                            } catch (e: Exception) {
                                Timber.e(e)
                                return@withLock
                            }

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

            pagination.observe(observer)
        } else {
            doSubscribe(null, null) {}
        }

        // Unsubscribe after the send channel
        // gets closed.
        channel.invokeOnClose {
            GlobalScope.launchUI {
                doDispose()
            }
        }
    }

    return channel
}

// Subscribe to single value changes

internal suspend inline fun <reified T : Any> DocumentReference.toValueChannel(exceptionHandler: ExceptionHandler): ReceiveChannel<T?> {
    forDebug { Timber.i("#LISTEN# toValueChannel $path") }

    val channel = Channel<T?>(Channel.RENDEZVOUS)

    val queue = ArrayList<DocumentSnapshot?>()
    val queueMutex = Mutex()
    val workerMutex = Mutex()

    val rootCoroutineScope = coroutineContext.toCoroutineScope()

    lateinit var registration: ListenerRegistration

    val listener = object : EventListener<DocumentSnapshot> {
        override fun onEvent(newSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
            if (e != null) {
                channel.close(exceptionHandler.handle(e, path))
                return
            }

            // Add a snapshot to process to a
            // queue.
            runBlocking {
                queueMutex.withLock {
                    queue += newSnapshot
                }
            }

            rootCoroutineScope.launchWorker {
                if (channel.isClosedForSend) {
                    return@launchWorker
                }

                workerMutex.withLock {
                    // Get the last data snapshot from
                    // a queue.
                    val data = queueMutex.withLock {
                        queue
                            .lastOrNull()
                            .also {
                                queue.clear()
                            }
                    }

                    val single = try {
                        data?.mapSingle<T>()
                    } catch (e: Exception) {
                        null
                    }

                    try {
                        channel.send(single)
                    } catch (e: ClosedSendChannelException) {
                        launchUI {
                            registration.remove()
                        }
                    }
                }
            }
        }
    }

    rootCoroutineScope.launchUI {
        registration = addSnapshotListener(listener)

        // Unsubscribe after the send channel
        // gets closed.
        channel.invokeOnClose {
            registration.remove()
        }
    }

    return channel
}

internal suspend inline fun <reified T : Any> DocumentReference.toListChannel(exceptionHandler: ExceptionHandler): ReceiveChannel<List<T>> {
    forDebug { Timber.i("#LISTEN# toListChannel $path") }

    val channel = Channel<List<T>>(Channel.RENDEZVOUS)

    val queue = ArrayList<DocumentSnapshot?>()
    val queueMutex = Mutex()
    val workerMutex = Mutex()

    val rootCoroutineScope = coroutineContext.toCoroutineScope()

    lateinit var registration: ListenerRegistration

    val listener = object : EventListener<DocumentSnapshot> {
        override fun onEvent(newSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
            if (e != null) {
                channel.close(exceptionHandler.handle(e, path))
                return
            }

            // Add a snapshot to process to a
            // queue.
            runBlocking {
                queueMutex.withLock {
                    queue += newSnapshot
                }
            }

            rootCoroutineScope.launchWorker {
                if (channel.isClosedForSend) {
                    return@launchWorker
                }

                workerMutex.withLock {
                    // Get the last data snapshot from
                    // a queue.
                    val data = queueMutex.withLock {
                        queue
                            .lastOrNull()
                            .also {
                                queue.clear()
                            }
                    }

                    val list = try {
                        data?.mapList<T>()
                    } catch (e: Exception) {
                        null
                    }
                    // Fallback to empty list
                        ?: emptyList()

                    try {
                        channel.send(list)
                    } catch (e: ClosedSendChannelException) {
                        launchUI {
                            registration.remove()
                        }
                    }
                }
            }
        }
    }

    rootCoroutineScope.launchUI {
        registration = addSnapshotListener(listener)

        // Unsubscribe after the send channel
        // gets closed.
        channel.invokeOnClose {
            registration.remove()
        }
    }

    return channel
}

internal suspend inline fun <reified T : Any> CollectionReference.toListChannel(exceptionHandler: ExceptionHandler): ReceiveChannel<List<T>> {
    forDebug { Timber.i("#LISTEN# toListChannel $path") }

    val channel = Channel<List<T>>(Channel.RENDEZVOUS)

    val queue = ArrayList<QuerySnapshot?>()
    val queueMutex = Mutex()
    val workerMutex = Mutex()

    val rootCoroutineScope = coroutineContext.toCoroutineScope()

    lateinit var registration: ListenerRegistration

    val listener = object : EventListener<QuerySnapshot> {
        override fun onEvent(newSnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
            if (e != null) {
                channel.close(exceptionHandler.handle(e, path))
                return
            }

            // Add a snapshot to process to a
            // queue.
            runBlocking {
                queueMutex.withLock {
                    queue += newSnapshot
                }
            }

            rootCoroutineScope.launchWorker {
                if (channel.isClosedForSend) {
                    return@launchWorker
                }

                workerMutex.withLock {
                    // Get the last data snapshot from
                    // a queue.
                    val data = queueMutex.withLock {
                        queue
                            .lastOrNull()
                            .also {
                                queue.clear()
                            }
                    }

                    val list = try {
                        data?.mapList<T>()
                    } catch (e: Exception) {
                        null
                    }
                    // Fallback to empty list
                        ?: emptyList()

                    try {
                        channel.send(list)
                    } catch (e: ClosedSendChannelException) {
                        launchUI {
                            registration.remove()
                        }
                    }
                }
            }
        }
    }

    rootCoroutineScope.launchUI {
        registration = addSnapshotListener(listener)

        // Unsubscribe after the send channel
        // gets closed.
        channel.invokeOnClose {
            registration.remove()
        }
    }

    return channel
}

