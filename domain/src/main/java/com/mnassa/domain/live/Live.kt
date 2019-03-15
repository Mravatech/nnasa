package com.mnassa.domain.live

import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * @author Artem Chepurnoy
 */
abstract class Live<Observer> {

    @Volatile
    var isActive: Boolean = false
         private set(value) {
             field = value

             if (value) {
                 onActive()
             } else {
                 onInactive()
             }
         }

    private val monitor = Object()

    private val observers = ArrayList<Observer>()

    fun observe(observer: Observer) {
        synchronized(monitor) {
            if (observer in observers) {
                throw IllegalStateException("Duplicate observers are not supported")
            }

            observers += observer
            onObserverAdded(observer)
        }
    }

    fun removeObserver(observer: Observer) {
        synchronized(monitor) {
            observers -= observer
            onObserverRemoved(observer)
        }
    }

    protected open fun onObserverAdded(observer: Observer) {
        updateIsActive()
    }

    protected open fun onObserverRemoved(observer: Observer) {
        updateIsActive()
    }

    protected fun forEachObserver(block: (Observer) -> Unit) = observers.forEach(block)

    private fun updateIsActive() {
        val shouldBeActive = observers.size > 0
        if (shouldBeActive != isActive) {
            isActive = shouldBeActive
        }
    }

    abstract fun onActive()

    abstract fun onInactive()

}

suspend fun <Observer> Live<Observer>.consume(observer: Observer) {
    suspendCancellableCoroutine<Unit> { continuation ->
        observe(observer)

        // Unsubscribe on coroutine
        // cancelation.
        continuation.invokeOnCancellation {
            removeObserver(observer)
        }
    }
}
