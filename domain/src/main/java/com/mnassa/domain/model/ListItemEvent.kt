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

    class Added<T : Any>(item: T, previousChildName: String? = null) : ListItemEvent<T>(item)
    class Moved<T : Any>(item: T, previousChildName: String? = null) : ListItemEvent<T>(item)
    class Changed<T : Any>(item: T, previousChildName: String? = null) : ListItemEvent<T>(item)
    class Removed<T : Any>(item: T) : ListItemEvent<T>(item)
    class Cleared<T : Any> : ListItemEvent<T>()

    override fun toString(): String {
        return "${super.toString()}; item: ${if (this::item.isInitialized) item else "_UNINITIALIZED_"}"
    }
}

suspend fun <E : Any> ReceiveChannel<ListItemEvent<E>>.bufferize(
        subscriptionContainer: SubscriptionContainer,
        bufferizationTimeMillis: Long = 2_000L
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

        val sendItems = suspend {
            if (addItemsCache.isNotEmpty()) {
                outputChannel.send(ListItemEvent.Added(addItemsCache.toMutableList()))
                addItemsCache.clear()
            }
        }

        consumeEach {
            when (it) {
                is ListItemEvent.Added -> {
                    addItemsCache.add(it.item)

                    if (System.currentTimeMillis() - lastItemsSentAt < bufferizationTimeMillis) {
                        sendItemsJob?.cancel()
                    }

                    sendItemsJob = subscriptionContainer.launchCoroutineUI {
                        delay(bufferizationTimeMillis)
                        sendItems()
                        lastItemsSentAt = System.currentTimeMillis()
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