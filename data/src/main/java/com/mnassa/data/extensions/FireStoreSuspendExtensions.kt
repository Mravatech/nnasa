package com.mnassa.data.extensions

import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Created by Peter on 4/19/2018.
 */
internal suspend inline fun <reified T : Any> DocumentReference.await(): T? {
    forDebug { Timber.i("#LISTEN# await ${this.path}") }
    return internalAwait<T?, DocumentSnapshot>(
        { addSnapshotListener(it) },
        {
            try {
                it?.mapSingle()
            } catch (e: Exception) {
                Timber.e(e)
                null
            }
        }
    )
}

internal suspend inline fun <reified T : Any> DocumentReference.awaitList(): List<T> {
    forDebug { Timber.i("#LISTEN# awaitList ${this.path}") }
    return internalAwait<List<T>, DocumentSnapshot>(
        { addSnapshotListener(it) },
        {
            it.mapList()
        }
    )
}

internal suspend inline fun <reified T : Any> Query.awaitList(): List<T> {
    forDebug { Timber.i("#LISTEN# awaitList ${this}") }
    return internalAwait<List<T>, QuerySnapshot>(
        { addSnapshotListener(it) },
        {
            it.mapList()
        }
    )
}

private suspend inline fun <T, L> internalAwait(
    crossinline subscribe: (EventListener<L>) -> ListenerRegistration,
    crossinline transform: (L?) -> T
): T {
    return withContext(Dispatchers.Main) {
        suspendCancellableCoroutine<T> { continuation ->
            lateinit var registration: ListenerRegistration

            val listener = object : EventListener<L> {
                override fun onEvent(snapshot: L?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        continuation.resumeWithException(e)
                        return
                    }

                    registration.remove() // we should wait only for one event

                    if (continuation.isActive) {
                        val value = snapshot.let(transform)
                        continuation.resume(value)
                    }
                }
            }

            registration = subscribe(listener)

            continuation.invokeOnCancellation {
                registration.remove()
            }
        }
    }
}
