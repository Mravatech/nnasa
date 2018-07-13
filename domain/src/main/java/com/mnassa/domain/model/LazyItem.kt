package com.mnassa.domain.model

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.io.Serializable

/**
 * Created by Peter on 7/13/2018.
 */
abstract class LazyItem<T>(val id: String) : Serializable where T : Serializable {
    var isReady: Boolean = false
        private set(value) {
            field = value
            if (!value) lastFuture = null
        }
    var item: T? = null
        private set(value) {
            field = value
        }
    @Transient
    private var lastFuture: Deferred<T?>? = null

    fun prepare(): Deferred<T?> {
        lastFuture?.let { return it }
        return if (isReady) CompletableDeferred(item)
        else async { prepareSuspend() }.also { lastFuture = it }
    }

    private suspend fun prepareSuspend(): T? {
        if (isReady) return item
        item = loadItem(id)
        isReady = true
        return item
    }

    fun update(): Deferred<T?> {
        isReady = false
        return prepare()
    }

    abstract suspend fun loadItem(id: String): T?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LazyItem<*>

        if (id != other.id) return false
        if (isReady != other.isReady) return false
        if (item != other.item) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + isReady.hashCode()
        result = 31 * result + (item?.hashCode() ?: 0)
        return result
    }
}