package com.mnassa.domain.pagination

import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Artem Chepurnoy
 */
class PaginationController(
    var size: Long = 40
) {

    private val observers = CopyOnWriteArrayList<PaginationObserver>()

    /**
     * Registers pagination observer to listen for [nextPage]
     * requests.
     * @see removeObserver
     */
    fun observe(observer: PaginationObserver) {
        observers += observer
        observer.invoke(size)
    }

    fun removeObserver(observer: PaginationObserver) {
        observers -= observer
    }

    fun nextPage(pageSize: Long) {
        size += pageSize

        // Notify all of the observers about
        // size change.
        tweet {
            invoke(size)
        }
    }

    private inline fun tweet(crossinline block: PaginationObserver.() -> Unit) {
        observers.forEach {
            block(it)
        }
    }

}