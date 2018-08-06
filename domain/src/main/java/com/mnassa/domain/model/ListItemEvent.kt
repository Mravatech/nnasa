package com.mnassa.domain.model

import com.mnassa.core.addons.launchWorker
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay

sealed class ListItemEvent<T : Any>() {
    lateinit var item: T

    constructor(item: T) : this() {
        this.item = item
    }

    class Added<T : Any>(item: T, previousChildName: String? = null) : ListItemEvent<T>(item) {
        override fun toBatched(): ListItemEvent<List<T>> = Added(listOf(item))
    }

    class Moved<T : Any>(item: T, previousChildName: String? = null) : ListItemEvent<T>(item) {
        override fun toBatched(): ListItemEvent<List<T>> = Moved(listOf(item))
    }

    class Changed<T : Any>(item: T, previousChildName: String? = null) : ListItemEvent<T>(item) {
        override fun toBatched(): ListItemEvent<List<T>> = Changed(listOf(item))
    }

    class Removed<T : Any>(item: T) : ListItemEvent<T>(item) {
        override fun toBatched(): ListItemEvent<List<T>> = Removed(listOf(item))
    }

    class Cleared<T : Any> : ListItemEvent<T>() {
        override fun toBatched(): ListItemEvent<List<T>> = Cleared()
    }

    override fun toString(): String {
        return "${super.toString()}; item: ${if (this::item.isInitialized) item else "_UNINITIALIZED_"}"
    }

    abstract fun toBatched(): ListItemEvent<List<T>>
}

suspend fun <E : Any> ReceiveChannel<ListItemEvent<E>>.withBuffer(bufferWindow: Long = 2000, sendIfEmpty: Boolean = false): ReceiveChannel<ListItemEvent<List<E>>> {
    val input = this

    return produce(context = Unconfined) {
        val output = this
        val eventsBuffer = ArrayList<E>()
        suspend fun flush() {
            if (output.isActive && (sendIfEmpty || eventsBuffer.isNotEmpty())) {
                val event: ListItemEvent<List<E>> = ListItemEvent.Added(eventsBuffer.toMutableList())
                eventsBuffer.clear()
                send(event)
            }
        }

        launchWorker {
            while (output.isActive) {
                delay(bufferWindow)
                flush()
            }
            input.cancel()
        }

        input.consumeEach {
            if (!output.isActive) {
                return@consumeEach
            }
            when (it) {
                is ListItemEvent.Added -> eventsBuffer.add(it.item)
                is ListItemEvent.Changed -> eventsBuffer.add(it.item)
                is ListItemEvent.Moved -> eventsBuffer.add(it.item)
                is ListItemEvent.Removed -> {
                    flush()
                    send(it.toBatched())
                }
                is ListItemEvent.Cleared -> {
                    eventsBuffer.clear()
                    send(it.toBatched())
                }
            }
        }
    }
}