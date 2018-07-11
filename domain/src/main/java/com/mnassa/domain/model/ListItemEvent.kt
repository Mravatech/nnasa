package com.mnassa.domain.model

import com.mnassa.core.addons.SubscriptionContainer
import com.mnassa.core.addons.launchCoroutineUI
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import kotlinx.coroutines.experimental.channels.consumeEach
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

suspend fun <E : Any> ReceiveChannel<ListItemEvent<E>>.bufferize(
        subscriptionContainer: SubscriptionContainer,
        bufferizationTimeMillis: Long = 2000L,
        maxBufferSize: Int = 500
): ReceiveChannel<ListItemEvent<List<E>>> {
    val srcChannel = this
    var sendItemsJob: Job? = null
    lateinit var consumeJob: Job
    var lastItemsSentAt: Long = System.currentTimeMillis()

    val outputChannel = object : RendezvousChannel<ListItemEvent<List<E>>>() {
        override fun cancel(cause: Throwable?): Boolean {
            srcChannel.cancel(cause)
            sendItemsJob?.cancel()
            consumeJob.cancel()
            return super.cancel(cause)
        }
    }

    consumeJob = subscriptionContainer.launchCoroutineUI {
        val addItemsCache: MutableList<E> = ArrayList()

        lateinit var sendItems: suspend () -> Unit

        sendItems = suspend {
            outputChannel.send(ListItemEvent.Added(addItemsCache.toMutableList()))
            addItemsCache.clear()
            lastItemsSentAt = System.currentTimeMillis()

            sendItemsJob?.cancel()
            sendItemsJob = subscriptionContainer.launchCoroutineUI {
                delay(bufferizationTimeMillis)
                if (isActive) {
                    sendItems()
                }
            }
        }

        //emit empty list when no items consumed
        sendItemsJob = subscriptionContainer.launchCoroutineUI {
            delay(bufferizationTimeMillis)
            if (this.isActive) {
                sendItems()
            }

        }

        consumeEach {
            when (it) {
                is ListItemEvent.Added -> {
                    addItemsCache.add(it.item)

                    if (System.currentTimeMillis() - lastItemsSentAt < bufferizationTimeMillis && addItemsCache.size < maxBufferSize) {
                        sendItemsJob?.cancel()
                        sendItemsJob = subscriptionContainer.launchCoroutineUI {
                            delay(bufferizationTimeMillis)
                            sendItems()
                        }
                    } else {
                        sendItems()
                    }
                }
                is ListItemEvent.Changed -> {
                    sendItems()
                    outputChannel.send(ListItemEvent.Changed(listOf(it.item)))
                }
                is ListItemEvent.Moved -> {
                    sendItems()
                    outputChannel.send(ListItemEvent.Moved(listOf(it.item)))
                }
                is ListItemEvent.Removed -> {
                    sendItems()
                    outputChannel.send(ListItemEvent.Removed(listOf(it.item)))
                }
                is ListItemEvent.Cleared -> {
                    addItemsCache.clear()
                    outputChannel.send(ListItemEvent.Cleared())
                }
            }
            Unit
        }
    }

    return outputChannel
}