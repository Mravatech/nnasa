package com.mnassa.domain.live

import java.util.*

typealias EventObserver<T> = (T) -> Boolean

/**
 * @author Artem Chepurnoy
 */
class EventLive<T> : Live<EventObserver<T>>() {

    private val queue = LinkedList<T>()

    override fun onActive() {
        // this is synchronized!
        if (queue.isNotEmpty()) {
            queue.forEach(::forEachObserverReversed)
            queue.clear()
        }
    }

    override fun onInactive() {
    }

    fun push(value: T) {
        synchronized(monitor) {
            if (isActive) {
                forEachObserverReversed(value)
            } else {
                // Hold this value until someone
                // consumes it.
                queue += value
            }
        }
    }

    private fun forEachObserverReversed(value: T) {
        forEachObserver(
            iteratorFactory = { it.reversed().iterator() },
            block = { it.invoke(value) }
        )
    }

}