package com.mnassa.data.extensions

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber

/**
 * Created by Peter on 4/19/2018.
 */
internal suspend inline fun <reified T : Any> DocumentReference.await(): T? {
    val result = firestoreLockSuspend {
        suspendCancellableCoroutine<T?> { continuation ->
            lateinit var listener: ListenerRegistration
            listener = addSnapshotListener { snapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    continuation.resumeWithException(it)
                    return@addSnapshotListener
                }
                try {
                    continuation.resume(snapshot.mapSingle())
                } catch (e: Exception) {
                    Timber.e(e)
                    continuation.resumeWithException(e)
                }
            }
            continuation.invokeOnCompletion {
                listener.remove()
            }
        }
    }
    return result
}

internal suspend inline fun <reified T : Any> DocumentReference.awaitList(): List<T> {
    val result = firestoreLockSuspend {
        suspendCancellableCoroutine<List<T>> { continuation ->
            lateinit var listener: ListenerRegistration
            listener = addSnapshotListener { snapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    continuation.resumeWithException(it)
                    return@addSnapshotListener
                }
                try {
                    continuation.resume(snapshot.mapList())
                } catch (e: Exception) {
                    Timber.e(e)
                    continuation.resumeWithException(e)
                }
            }
            continuation.invokeOnCompletion {
                listener.remove()
            }
        }
    }
    return result
}

internal suspend inline fun <reified T : Any> Query.awaitList(): List<T> {
    val result = firestoreLockSuspend {
        suspendCancellableCoroutine<List<T>> { continuation ->
            lateinit var listener: ListenerRegistration
            listener = addSnapshotListener { snapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    continuation.resumeWithException(it)
                    return@addSnapshotListener
                }
                try {
                    continuation.resume(snapshot.mapList())
                } catch (e: Exception) {
                    Timber.e(e)
                    continuation.resumeWithException(e)
                }
            }
            continuation.invokeOnCompletion {
                listener.remove()
            }
        }
    }
    return result
}