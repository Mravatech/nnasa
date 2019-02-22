package com.mnassa.domain.model

import com.mnassa.core.addons.launchWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.consumes
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed class ListItemEvent<T : Any>() {
    lateinit var item: T
    var previousChildName: String? = null

    constructor(item: T) : this() {
        this.item = item
        this.previousChildName = null
    }

    constructor(previousChildName: String): this() {
        this.previousChildName = previousChildName
    }

    class Added<T : Any>(item: T) : ListItemEvent<T>(item) {
        override fun toBatched(): ListItemEvent<List<T>> = Added(listOf(item))
    }

    class Moved<T : Any>(item: T) : ListItemEvent<T>(item) {
        override fun toBatched(): ListItemEvent<List<T>> = Moved(listOf(item))
    }

    class Changed<T : Any>(item: T) : ListItemEvent<T>(item) {
        override fun toBatched(): ListItemEvent<List<T>> = Changed(listOf(item))
    }

    class Removed<T : Any> : ListItemEvent<T> {
        constructor(item: T): super(item)
        constructor(previousChildName: String): super(previousChildName = previousChildName)

        override fun toBatched(): ListItemEvent<List<T>> = if (previousChildName != null) Removed(previousChildName!!) else Removed(listOf(item))
    }

    class Cleared<T : Any> : ListItemEvent<T>() {
        override fun toBatched(): ListItemEvent<List<T>> = Cleared()
    }

    override fun toString(): String {
        return "${super.toString()}; item: ${if (this::item.isInitialized) item else "_UNINITIALIZED_"}"
    }

    abstract fun toBatched(): ListItemEvent<List<T>>
}

@UseExperimental(InternalCoroutinesApi::class)
suspend fun <E : Any> ReceiveChannel<ListItemEvent<E>>.withBuffer(
    bufferWindow: Long = 2000,
    sendIfEmpty: Boolean = false
): ReceiveChannel<ListItemEvent<List<E>>> {
    val input = this@withBuffer
    val mutex = Mutex()

    return GlobalScope.produce(context = Dispatchers.Unconfined, onCompletion = consumes()) {
        val output = this
        val buffer = ArrayList<E>()

        suspend fun flush() {
            if (output.isActive) {
                mutex.withLock {
                    if (sendIfEmpty || buffer.isNotEmpty()) {
                        // Send buffered added items all
                        // at once.
                        val event = ListItemEvent.Added(buffer.toList())
                        output.send(event)

                        buffer.clear()
                    }
                }
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
            if (output.isActive) when (it) {
                is ListItemEvent.Added,
                is ListItemEvent.Changed,
                is ListItemEvent.Moved -> {
                    mutex.withLock {
                        buffer.add(it.item)
                    }
                }
                is ListItemEvent.Removed -> {
                    // Send buffered added items before
                    // removing this one.
                    flush()

                    output.send(it.toBatched())
                }
                is ListItemEvent.Cleared -> {
                    mutex.withLock {
                        buffer.clear()
                    }

                    output.send(it.toBatched())
                }
            }
        }
    }
}