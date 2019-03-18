package com.mnassa.core.live

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

    protected val monitor = Object()

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

    protected fun <T> forEachObserver(
        iteratorFactory: (Iterable<Observer>) -> Iterator<Observer> = { it.iterator() },
        block: (Observer) -> T
    ) {
        synchronized(monitor) {
            val iterator = iteratorFactory(observers)
            while (iterator.hasNext()) {
                val shouldBreak = block(iterator.next()) as? Boolean ?: false
                if (shouldBreak) {
                    break
                }
            }
        }
    }

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
