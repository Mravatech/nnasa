package com.mnassa.data.extensions

import com.google.firebase.database.*
import com.mnassa.core.addons.launchUI
import com.mnassa.core.addons.launchWorker
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.model.HasId
import com.mnassa.domain.model.ListItemEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

// Beware, stranger, you're entering the zone
// of async stuff.

private const val EVENT_MOVED = 1
private const val EVENT_CHANGED = 2
private const val EVENT_ADDED = 3
private const val EVENT_REMOVED = 4

private const val BATCH_SIZE = 10

internal suspend inline fun <reified T : Any> Query.toListChannel(
    exceptionHandler: ExceptionHandler
): ReceiveChannel<List<T>> {
    forDebug { Timber.i("#LISTEN# toListChannel $ref") }

    val channel = Channel<List<T>>(Channel.RENDEZVOUS)

    val queue = ArrayList<DataSnapshot>()
    val queueMutex = Mutex()
    val workerMutex = Mutex()

    val rootCoroutineContext = coroutineContext
    val rootCoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = rootCoroutineContext

    }

    val listener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            channel.close(exceptionHandler.handle(error.toException(), ref.path.toString()))
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val listener = this

            // Add a snapshot to process to a
            // queue.
            runBlocking {
                queueMutex.withLock {
                    queue += dataSnapshot
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
                        ?: return@launchWorker // nothing to process

                    val list = data.mapList<T>()
                    try {
                        channel.send(list)
                    } catch (e: ClosedSendChannelException) {
                        launchUI {
                            removeEventListener(listener)
                        }
                    }
                }
            }
        }
    }

    rootCoroutineScope.launchUI {
        addValueEventListener(listener)

        // Unsubscribe after the send channel
        // gets closed.
        channel.invokeOnClose {
            removeEventListener(listener)
        }
    }

    return channel
}

internal suspend inline fun <reified DbType : HasId, reified OutType : Any> Query.toListItemEventChannel(
    noinline mapper: suspend (DbType) -> OutType? = { it as OutType },
    exceptionHandler: ExceptionHandler
): ReceiveChannel<ListItemEvent<OutType>> {
    forDebug { Timber.i("#LISTEN# toListItemEventChannel $ref") }

    val channel = Channel<ListItemEvent<OutType>>(Channel.RENDEZVOUS)

    val queue = ArrayList<Pair<DataSnapshot, Int>>()
    val queueMutex = Mutex()
    val workerMutex = Mutex()

    val rootCoroutineContext = coroutineContext
    val rootCoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = rootCoroutineContext

    }

    val doProcess = { listener: ChildEventListener, newSnapshot: DataSnapshot, eventType: Int ->
        // Add a snapshot to process to a
        // queue.
        runBlocking {
            queueMutex.withLock {
                queue += newSnapshot to eventType
            }
        }

        rootCoroutineScope.launchWorker {
            if (channel.isClosedForSend) {
                return@launchWorker
            }

            workerMutex.withLock {

                // Get the first few snapshot events from
                // a queue.
                val data = queueMutex.withLock {
                    queue
                        .take(BATCH_SIZE)
                        .also {
                            // Remove pop-ed events
                            for (i in 0 until BATCH_SIZE) {
                                if (queue.isEmpty()) {
                                    break
                                }

                                queue.removeAt(0)
                            }
                        }
                }
                    // Start parsing all of the items
                    // at once.
                    .map { (snapshot, eventType) ->
                        Triple(
                            first = snapshot.key,
                            second = async(Dispatchers.Default) {
                                val dbEntity = try {
                                    snapshot.mapSingle<DbType>()
                                } catch (e: Exception) {
                                    Timber.e(e)
                                    null
                                }

                                return@async dbEntity?.let { mapper(it) }
                            },
                            third = eventType
                        )
                    }

                data.forEach { (key, task, eventType) ->
                    // Wait until the model
                    // loads
                    val model = task.await()

                    // Send the event to a channel
                    val result: ListItemEvent<OutType>? = when (eventType) {
                        EVENT_MOVED -> model?.let { ListItemEvent.Moved(it) }
                        EVENT_CHANGED -> model?.let { ListItemEvent.Changed(it) }
                        EVENT_ADDED -> model?.let { ListItemEvent.Added(it) }
                        EVENT_REMOVED -> key?.let { ListItemEvent.Removed<OutType>(it) }
                        else -> throw IllegalArgumentException("Illegal event type $eventType")
                    }

                    if (result != null) try {
                        channel.send(result)
                    } catch (e: ClosedSendChannelException) {
                        launchUI {
                            removeEventListener(listener)
                        }

                        return@withLock
                    }
                }
            }
        }
    }

    val listener = addChildEventListener(object : ChildEventListener {
        override fun onCancelled(error: DatabaseError) {
            channel.close(exceptionHandler.handle(error.toException(), ref.path.toString()))
        }

        override fun onChildMoved(snapshot: DataSnapshot, prevChildKey: String?) {
            doProcess(this, snapshot, EVENT_MOVED)
        }

        override fun onChildChanged(snapshot: DataSnapshot, prevChildKey: String?) {
            doProcess(this, snapshot, EVENT_CHANGED)
        }

        override fun onChildAdded(snapshot: DataSnapshot, prevChildKey: String?) {
            doProcess(this, snapshot, EVENT_ADDED)
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            doProcess(this, snapshot, EVENT_REMOVED)
        }
    })

    rootCoroutineScope.launchUI {
        addChildEventListener(listener)

        // Unsubscribe after the send channel
        // gets closed.
        channel.invokeOnClose {
            removeEventListener(listener)
        }
    }

    return channel
}

internal suspend inline fun <reified T : Any> Query.toValueChannel(
    exceptionHandler: ExceptionHandler
): ReceiveChannel<T?> {
    forDebug { Timber.i("#LISTEN# toValueChannel $ref") }

    val channel = Channel<T?>(Channel.RENDEZVOUS)

    val queue = ArrayList<DataSnapshot>()
    val queueMutex = Mutex()
    val workerMutex = Mutex()

    val rootCoroutineContext = coroutineContext
    val rootCoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = rootCoroutineContext

    }

    val listener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            channel.close(exceptionHandler.handle(error.toException(), ref.path.toString()))
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val listener = this

            // Add a snapshot to process to a
            // queue.
            runBlocking {
                queueMutex.withLock {
                    queue += dataSnapshot
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
                        ?: return@launchWorker // nothing to process

                    val single = try {
                        data.mapSingle<T>()
                    } catch (e : Exception) {
                        null
                    }

                    try {
                        channel.send(single)
                    } catch (e: ClosedSendChannelException) {
                        launchUI {
                            removeEventListener(listener)
                        }
                    }
                }
            }
        }
    }

    rootCoroutineScope.launchUI {
        addValueEventListener(listener)

        // Unsubscribe after the send channel
        // gets closed.
        channel.invokeOnClose {
            removeEventListener(listener)
        }
    }

    return channel
}
