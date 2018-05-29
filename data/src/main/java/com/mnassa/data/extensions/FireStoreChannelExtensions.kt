package com.mnassa.data.extensions

import com.google.firebase.firestore.*
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.exception.FirebaseMappingException
import com.mnassa.domain.model.HasId
import com.mnassa.domain.model.ListItemEvent
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch
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
internal inline fun <reified DbType : HasId, reified OutType : Any> CollectionReference.toValueChannelWithChangesHandling(
        exceptionHandler: ExceptionHandler,
        noinline mapper: suspend (DbType) -> OutType = { it as OutType },
        limit: Int = DEFAULT_LIMIT): Channel<ListItemEvent<OutType>> {
    val channel = ArrayChannel<ListItemEvent<OutType>>(limit)

    lateinit var listener: ListenerRegistration

    val MOVED = 1
    val CHANGED = 2
    val ADDED = 3
    val REMOVED = 4

    val emitter = { input: QueryDocumentSnapshot, previousChildName: String?, eventType: Int ->
        launch {
            try {
                val dbEntity = input.mapSingle<DbType>() ?: return@launch
                val outModel = mapper(dbEntity)
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
                    e is ClosedSendChannelException -> listener.remove()
                    e.isSuppressed -> {
                        Timber.e(e, "Mapping exception: class: ${DbType::class.java.name} id: ${input.id}")
//                        removeEventListener(listener)
//                        channel.close(exceptionHandler.handle(FirebaseMappingException(input.path, e)))
                    }
                    else -> {
                        Timber.e(e)
                        listener.remove()
                        channel.close(exceptionHandler.handle(e))
                    }
                }
            }
        }
    }

    listener = addSnapshotListener { dataSnapshot, firebaseFirestoreException ->
        if (firebaseFirestoreException != null) {
            channel.close(exceptionHandler.handle(firebaseFirestoreException))
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

    return channel
}

// Subscribe to single value changes

internal inline fun <reified T : Any> DocumentReference.toValueChannel(exceptionHandler: ExceptionHandler): ReceiveChannel<T?> {
    val channel = RendezvousChannel<T?>()
    lateinit var listener: ListenerRegistration

    listener = addSnapshotListener { dataSnapshot, firebaseFirestoreException ->
        if (firebaseFirestoreException != null) {
            channel.close(exceptionHandler.handle(firebaseFirestoreException))
            listener.remove()
            return@addSnapshotListener
        }

        launch {
            try {
                channel.send(dataSnapshot.mapSingle())
            } catch (e: ClosedSendChannelException) {
                listener.remove()
            } catch (e: Exception) {
                Timber.e(e)
                listener.remove()
                channel.close(exceptionHandler.handle(e))
            }
        }
    }

    return channel
}

internal inline fun <reified T : Any> DocumentReference.toListChannel(exceptionHandler: ExceptionHandler): ReceiveChannel<List<T>> {
    val channel = RendezvousChannel<List<T>>()
    lateinit var listener: ListenerRegistration

    listener = addSnapshotListener { dataSnapshot, firebaseFirestoreException ->
        if (firebaseFirestoreException != null) {
            channel.close(exceptionHandler.handle(firebaseFirestoreException))
            listener.remove()
            return@addSnapshotListener
        }

        launch {
            try {
                channel.send(dataSnapshot.mapList())
            } catch (e: ClosedSendChannelException) {
                listener.remove()
            } catch (e: Exception) {
                Timber.e(e)
                listener.remove()
                channel.close(exceptionHandler.handle(e))
            }
        }
    }

    return channel
}

internal inline fun <reified T : Any> CollectionReference.toListChannel(exceptionHandler: ExceptionHandler): ReceiveChannel<List<T>> {
    val channel = RendezvousChannel<List<T>>()
    lateinit var listener: ListenerRegistration

    listener = addSnapshotListener { dataSnapshot, firebaseFirestoreException ->
        if (firebaseFirestoreException != null) {
            channel.close(exceptionHandler.handle(firebaseFirestoreException))
            listener.remove()
            return@addSnapshotListener
        }

        launch {
            try {
                channel.send(dataSnapshot.mapList())
            } catch (e: ClosedSendChannelException) {
                listener.remove()
            } catch (e: Exception) {
                Timber.e(e)
                listener.remove()
                channel.close(exceptionHandler.handle(e))
            }
        }
    }

    return channel
}

