package com.mnassa.domain.pagination

import android.os.SystemClock
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Artem Chepurnoy
 */
class PaginationController(
    var size: Long = 40
) {

    companion object {
        private val MIN_UPDATE_TIME = 2000L
    }

    private val observers = CopyOnWriteArrayList<PaginationObserver>()

    /**
     * `true` if any of the observers are
     * in the "busy" state.
     */
    private val isBusy: Boolean
        get() {
            observers.forEach {
                if (it.isBusy) return true
            }
            return false
        }

    /**
     * `true` if any of the observers were
     * completed.
     */
    private val isCompleted: Boolean
        get() {
            observers.forEach {
                if (it.isCompleted) return true
            }
            return false
        }

    private var lastNextPageTimestamp = 0L

    /**
     * Registers pagination observer to listen for [requestNextPage]
     * requests.
     * @see removeObserver
     */
    fun observe(observer: PaginationObserver) {
        observers += observer
        observer.onNextPageRequested(size)
    }

    fun removeObserver(observer: PaginationObserver) {
        observers -= observer
    }

    fun requestNextPage(pageSize: Long) {
        val now = SystemClock.elapsedRealtime()
        if (isCompleted || isBusy || now - lastNextPageTimestamp < MIN_UPDATE_TIME) {
            return
        }

        lastNextPageTimestamp = now
        size += pageSize

        // Notify all of the observers about
        // size change.
        tweet {
            onNextPageRequested(pageSize)
        }
    }

    private inline fun tweet(crossinline block: PaginationObserver.() -> Unit) {
        observers.forEach {
            block(it)
        }
    }

}