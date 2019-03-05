package com.mnassa.data.cache

import android.os.SystemClock
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.extensions.toCoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlin.coroutines.coroutineContext

/**
 * @author Artem Chepurnoy
 */
class Cached<T>(
    @Volatile
    private var field: T? = null
) {

    /**
     * Time of the previous
     * update.
     */
    @Volatile
    private var updateTime = 0L

    private inline val now: Long
        get() = SystemClock.elapsedRealtime()

    suspend fun getOr(allowOld: Boolean = true, getter: suspend () -> T): T {
        val value = field

        val shouldRefresh = value?.takeIf { now - updateTime < CACHE_TIMEOUT } == null
        if (shouldRefresh) {
            suspend fun performUpdate() =
                getter().also {
                    field = it
                    updateTime = now
                }

            if (value == null || !allowOld) {
                return performUpdate()
            }

            val scope = coroutineContext.toCoroutineScope()
            scope.launchWorker {
                performUpdate()
            }

            return value
        } else {
            return value!!
        }
    }

    companion object {
        private const val CACHE_TIMEOUT = 1000L * 60L * 30L // 30m
    }

}
