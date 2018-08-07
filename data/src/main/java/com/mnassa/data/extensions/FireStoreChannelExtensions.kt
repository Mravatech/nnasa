package com.mnassa.data.extensions

import com.google.firebase.firestore.*
import com.mnassa.core.addons.launchWorker
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.model.HasId
import com.mnassa.domain.model.ListItemEvent
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber

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
        noinline mapper: suspend (DbType) -> OutType? = { it as OutType },
        limit: Int = DEFAULT_LIMIT): Channel<ListItemEvent<OutType>> {
    forDebug { Timber.i("#LISTEN# toValueChannelWithChangesHandling ${this.path}") }
    val channel = ArrayChannel<ListItemEvent<OutType>>(limit)

    lateinit var listener: ListenerRegistration

    val MOVED = 1
    val CHANGED = 2
    val ADDED = 3
    val REMOVED = 4

    val emitter = { input: QueryDocumentSnapshot, previousChildName: String?, eventType: Int ->
        launchWorker {
            try {

                if (channel.isClosedForSend) {
                    listener.remove()
                    return@launchWorker
                }

                val dbEntity = input.mapSingle<DbType>() ?: return@launchWorker
                val outModel = withContext(DefaultDispatcher) { mapper(dbEntity) } ?: return@launchWorker
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
                        Timber.e(e, "Mapping exception: class: ${DbType::class.java.name} id: ${input.id}")
//                        removeEventListener(listener)
//                        channel.close(exceptionHandler.handle(FirebaseMappingException(input.path, e)))
                    }
                    else -> {
                        Timber.e(e)
                        listener.remove()
                        channel.close(exceptionHandler.handle(e, path))
                    }
                }
            }
        }
    }

    firestoreLockSuspend {
        listener = addSnapshotListener { dataSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                channel.close(exceptionHandler.handle(firebaseFirestoreException, path))
                listener.remove()
                return@addSnapshotListener
            }

            if (dataSnapshot == null) return@addSnapshotListener

            dataSnapshot.documentChanges.forEach {
                when (it.type) {
                    DocumentChange.Type.ADDED -> emitter(it.document, null, ADDED)
                    DocumentChange.Type.MODIFIED -> emitter(it.document, null, CHANGED)
                    DocumentChange.Type.REMOVED -> emitter(it.document, null, REMOVED)
                    else -> throw IllegalStateException("Illegal change type ${it.type}")
                }
            }
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

